package com.mesh_suite.service.user;

import com.mesh_suite.constant.forms.UserStatus;
import com.mesh_suite.dao.user.RoleRepository;
import com.mesh_suite.dao.user.UserRepository;
import com.mesh_suite.domain.user.Role;
import com.mesh_suite.domain.user.Users;
import com.mesh_suite.dto.request.ChangePasswordRequest;
import com.mesh_suite.dto.request.UpdateUserCompanyDTO;
import com.mesh_suite.dto.request.UserFilterRequest;
import com.mesh_suite.dto.response.MessageResponse;
import com.mesh_suite.dto.response.UserResponse;
import com.mesh_suite.exception.BadRequestException;
import com.mesh_suite.exception.ResourceNotFoundException;
import com.mesh_suite.exception.UnAuthenticatedException;
import com.mesh_suite.mapper.UsersMapper;
import com.mesh_suite.util.UserSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.mesh_suite.util.Constants.USER_NOT_FOUND_WITH_ID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UsersMapper userMapper;
    private final RoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse getUserById(Long id, UserDetails userDetails) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_WITH_ID + id));
        UserResponse response = userMapper.toUserResponse(user);
        return response;

    }

    public UserResponse getUserByEmail(String email, UserDetails userDetails) {
        try {
            Users user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
            UserResponse response = userMapper.toUserResponse(user);
            return response;
        } catch (Exception e) {

            throw e;
        }
    }

    public void setUserEnabledStatus(Long id, boolean enabled, UserDetails userDetails) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_WITH_ID + id));

        try {
            user.setStatus(enabled ? UserStatus.ACTIVE : UserStatus.INACTIVE);
            userRepository.save(user);

            log.info("User {}: {}", (enabled ? "enabled" : "disabled"), user.getEmail());
        } catch (Exception e) {

            log.error("Failed to {} user with id: {}", (enabled ? "enable" : "disable"), id, e);
            throw new UnAuthenticatedException("Failed to " + (enabled ? "enable" : "disable") + " user: " + e.getMessage());
        }
    }

    public void deleteUser(Long id, UserDetails userDetails) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_WITH_ID + id));

        try {
            userRepository.delete(user);

            log.info("User deleted: {}", user.getEmail());
        } catch (Exception e) {

            log.error("Failed to delete user with id: {}", id, e);
            throw new RuntimeException("Failed to delete user: " + e.getMessage());
        }
    }

    @Transactional
    public MessageResponse updateUserRole(Long userId, Long roleId, UserDetails userDetails) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Role newRole = userRoleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        try {
            user.setRole(newRole);
            userRepository.save(user);

            return MessageResponse.builder()
                    .message(String.format("User role updated to '%s' successfully", newRole.getRoleName()))
                    .build();
        } catch (Exception e) {

            throw e;
        }
    }

    public List<UserResponse> filterUsers(UserFilterRequest request, UserDetails userDetails) {
        try {
            List<Users> users = userRepository.findAll(
                    UserSpecification.filterUsers(
                            request.getRoles(),
                            request.getLocationIds(),
                            request.getCreatedAfter(),
                            request.getCreatedBefore(),
                            request.getEnabled()
                    )
            );

            List<UserResponse> response = users.stream()
                    .map(userMapper::toUserResponse)
                    .toList();

            return response;
        } catch (Exception e) {

            throw e;
        }
    }

    @Transactional
    public UserResponse updateCompanyIdentifier(Long userId, UpdateUserCompanyDTO request, UserDetails userDetails) {

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Add any business logic/validation here
        user.setCompanyIdentifier(request.getCompanyIdentifier());
        Users updatedUser = userRepository.save(user);

        return userMapper.toUserResponse(updatedUser);
    }

    public MessageResponse changeUserPassword(String email, ChangePasswordRequest request) {

        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new UnAuthenticatedException("Current password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirm password do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return new MessageResponse("Password changed successfully");
    }

}
