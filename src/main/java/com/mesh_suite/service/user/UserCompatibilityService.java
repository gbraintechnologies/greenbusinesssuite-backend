package com.mesh_suite.service.user;

import com.mesh_suite.constant.forms.UserStatus;
import com.mesh_suite.dao.user.PermissionRepository;
import com.mesh_suite.dao.user.RoleRepository;
import com.mesh_suite.dao.user.UserRepository;
import com.mesh_suite.domain.user.CustomProfileOwnerType;
import com.mesh_suite.domain.user.Permission;
import com.mesh_suite.domain.user.Role;
import com.mesh_suite.domain.user.Users;
import com.mesh_suite.dto.request.CustomFieldInput;
import com.mesh_suite.dto.request.LegacyUserWriteRequest;
import com.mesh_suite.dto.response.MessageResponse;
import com.mesh_suite.dto.response.UserResponse;
import com.mesh_suite.exception.BadRequestException;
import com.mesh_suite.exception.DuplicateResourceException;
import com.mesh_suite.exception.ResourceNotFoundException;
import com.mesh_suite.interceptor.TenantContext;
import com.mesh_suite.mapper.UsersMapper;
import com.mesh_suite.service.notify.EmailService;
import com.mesh_suite.util.CodeGenerator;
import com.mesh_suite.util.LegacyMaps;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserCompatibilityService {

    private static final long PROFILE_IMAGE_ITEM_ID = 1L;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UsersMapper usersMapper;
    private final CustomProfileService customProfileService;
    private final PasswordEncoder passwordEncoder;
    private final CodeGenerator codeGenerator;
    private final EmailService emailService;

    public List<UserResponse> search(String word) {
        if (!StringUtils.hasText(word)) {
            return List.of();
        }
        return usersMapper.toUserResponses(userRepository.searchByWord(word.trim()));
    }

    public List<UserResponse> searchByEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return List.of();
        }
        return userRepository.findByEmailIgnoreCase(email.trim())
                .map(user -> List.of(usersMapper.toUserResponse(user)))
                .orElseGet(List::of);
    }

    @Transactional
    public MessageResponse blacklist(Long userId) {
        Users user = requireUser(userId);
        user.setStatus(UserStatus.BLACKLISTED);
        user.setEnabled(false);
        userRepository.save(user);
        return new MessageResponse("User blacklisted successfully");
    }

    @Transactional
    public UserResponse createWithCustomFields(LegacyUserWriteRequest request) {
        Map<String, Object> data = request == null || request.getUserData() == null ? Map.of() : request.getUserData();
        List<CustomFieldInput> customProfiles = request == null ? null : request.getCustomProfiles();

        String email = LegacyMaps.text(data, "email");
        if (!StringUtils.hasText(email)) {
            throw new BadRequestException("Email is required");
        }
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email is already in use");
        }

        String username = LegacyMaps.text(data, "username", "email");
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("Username is already in use");
        }

        Role role = resolveRole(LegacyMaps.number(data, "roleId", "role_id"));
        String rawPassword = codeGenerator.generateTemporaryPassword();
        String profileImage = customProfileService.valueFor(customProfiles, PROFILE_IMAGE_ITEM_ID);
        if (!StringUtils.hasText(profileImage)) {
            profileImage = LegacyMaps.text(data, "profile_image", "profileImage");
        }
        if (profileImage == null) {
            profileImage = "";
        }

        String tenantId = LegacyMaps.text(data, "tenantId", "tenant_id", "companyIdentifier", "company_identifier");
        if (!StringUtils.hasText(tenantId)) {
            tenantId = TenantContext.getCurrentTenant();
        }

        Users user = Users.builder()
                .firstName(LegacyMaps.text(data, "firstName", "first_name"))
                .lastName(LegacyMaps.text(data, "lastName", "last_name"))
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .temporaryPassword(rawPassword)
                .phoneNumber(LegacyMaps.text(data, "phone", "phone_number", "phoneNumber", "mobile_phone_number"))
                .profileImage(profileImage)
                .status(parseStatus(LegacyMaps.text(data, "status", "user_status")))
                .role(role)
                .roleName(role.getRoleName())
                .companyIdentifier(LegacyMaps.text(data, "companyIdentifier", "company_identifier", "tenantId", "tenant_id"))
                .tenantId(tenantId)
                .enabled(true)
                .isVerified(true)
                .createdOn(LocalDateTime.now())
                .build();

        Users saved = userRepository.saveAndFlush(user);
        customProfileService.replace(CustomProfileOwnerType.USER, saved.getId(), customProfiles);
        return usersMapper.toUserResponse(saved);
    }

    @Transactional
    public UserResponse editWithCustomFields(Long userId, LegacyUserWriteRequest request) {
        Users user = requireUser(userId);
        applyUserData(user, request == null ? null : request.getUserData());
        List<CustomFieldInput> fields = request == null ? null : request.getCustomProfiles();
        String profileImage = customProfileService.valueFor(fields, PROFILE_IMAGE_ITEM_ID);
        if (profileImage != null) {
            user.setProfileImage(profileImage);
        }
        userRepository.save(user);
        customProfileService.replace(CustomProfileOwnerType.USER, user.getId(), fields);
        return usersMapper.toUserResponse(user);
    }

    @Transactional
    public UserResponse update(Long userId, Map<String, Object> userData) {
        Users user = requireUser(userId);
        applyUserData(user, userData);
        userRepository.save(user);
        return usersMapper.toUserResponse(user);
    }

    @Transactional
    public MessageResponse replacePermissions(Long userId, List<Long> permissionIds) {
        Users user = requireUser(userId);
        Set<Long> requested = new LinkedHashSet<>(permissionIds == null ? List.of() : permissionIds);
        List<Permission> found = requested.isEmpty()
                ? List.of()
                : permissionRepository.findAllById(requested);
        if (found.size() != requested.size()) {
            throw new BadRequestException("One or more permissions were not found");
        }
        if (user.getDirectPermissions() == null) {
            user.setDirectPermissions(new HashSet<>());
        }
        user.getDirectPermissions().clear();
        user.getDirectPermissions().addAll(found);
        userRepository.save(user);
        return new MessageResponse("User permissions updated");
    }

    @Transactional(readOnly = true)
    public List<String> permissionNames(Long userId) {
        Users user = requireUser(userId);
        Set<String> names = new LinkedHashSet<>();
        if (user.getRole() != null && user.getRole().getPermissions() != null) {
            user.getRole().getPermissions().stream().map(Permission::getName).forEach(names::add);
        }
        if (user.getDirectPermissions() != null) {
            user.getDirectPermissions().stream().map(Permission::getName).forEach(names::add);
        }
        return List.copyOf(names);
    }

    public List<Map<String, Object>> customFieldCatalog() {
        List<Map<String, Object>> fields = new ArrayList<>();
        fields.add(catalogItem(1L, "profile_picture", "Profile picture"));
        return fields;
    }

    @Transactional
    public MessageResponse notifyTemporaryCredentials(Long userId, String channel) {
        if (userId == null) {
            throw new BadRequestException("user_id is required");
        }
        String normalizedChannel = channel == null ? "EMAIL" : channel.trim().toUpperCase(Locale.ROOT);
        if (!normalizedChannel.isEmpty() && !"EMAIL".equals(normalizedChannel)) {
            throw new BadRequestException("Only the EMAIL channel is supported");
        }
        Users user = requireUser(userId);
        if (!StringUtils.hasText(user.getTemporaryPassword())) {
            throw new BadRequestException("No temporary credentials are pending for this user");
        }
        String temporaryPassword = user.getTemporaryPassword();
        emailService.sendTemporaryPasswordEmail(user, temporaryPassword);
        user.setTemporaryPassword(null);
        userRepository.save(user);
        return new MessageResponse("Temporary credentials sent");
    }

    private void applyUserData(Users user, Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return;
        }
        String email = LegacyMaps.text(data, "email");
        if (StringUtils.hasText(email) && !email.equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmail(email)) {
                throw new DuplicateResourceException("Email is already in use");
            }
            user.setEmail(email);
        }
        String username = LegacyMaps.text(data, "username");
        if (StringUtils.hasText(username) && !username.equalsIgnoreCase(user.getUsername())) {
            if (userRepository.existsByUsername(username)) {
                throw new DuplicateResourceException("Username is already in use");
            }
            user.setUsername(username);
        }
        String firstName = LegacyMaps.text(data, "firstName", "first_name");
        if (firstName != null) {
            user.setFirstName(firstName);
        }
        String lastName = LegacyMaps.text(data, "lastName", "last_name");
        if (lastName != null) {
            user.setLastName(lastName);
        }
        String phone = LegacyMaps.text(data, "phone", "phone_number", "phoneNumber", "mobile_phone_number", "mobile_phone");
        if (phone != null) {
            user.setPhoneNumber(phone);
        }
        String status = LegacyMaps.text(data, "status", "user_status", "userStatus");
        if (status != null) {
            user.setStatus(parseStatus(status));
            user.setEnabled(user.getStatus() != UserStatus.BLACKLISTED && user.getStatus() != UserStatus.INACTIVE);
        }
        String companyIdentifier = LegacyMaps.text(data, "companyIdentifier", "company_identifier");
        if (companyIdentifier != null) {
            user.setCompanyIdentifier(companyIdentifier);
        }
        String profileImage = LegacyMaps.text(data, "profile_image", "profileImage");
        if (profileImage != null) {
            user.setProfileImage(profileImage);
        }
        Long roleId = LegacyMaps.number(data, "roleId", "role_id");
        if (roleId != null) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));
            user.setRole(role);
            user.setRoleName(role.getRoleName());
        }
    }

    private Role resolveRole(Long roleId) {
        if (roleId != null) {
            return roleRepository.findById(roleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));
        }
        return roleRepository.findByRoleNameIgnoreCase("CLIENT")
                .or(() -> roleRepository.findByRoleNameIgnoreCase("USER"))
                .or(() -> roleRepository.findAll().stream().findFirst())
                .orElseThrow(() -> new BadRequestException("No role is available to assign to the new user"));
    }

    private UserStatus parseStatus(String raw) {
        if (!StringUtils.hasText(raw)) {
            return UserStatus.ACTIVE;
        }
        try {
            return UserStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Unknown user status: " + raw);
        }
    }

    private Users requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private Map<String, Object> catalogItem(Long id, String key, String label) {
        return Map.of(
                "id", id,
                "item_key", key,
                "item_label", label,
                "custom_profile_item_id", id
        );
    }
}
