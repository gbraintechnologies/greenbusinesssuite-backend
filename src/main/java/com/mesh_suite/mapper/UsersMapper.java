package com.mesh_suite.mapper;

import com.mesh_suite.dao.user.CustomProfileValueRepository;
import com.mesh_suite.domain.user.CustomProfileOwnerType;
import com.mesh_suite.domain.user.CustomProfileValue;
import com.mesh_suite.domain.user.Role;
import com.mesh_suite.domain.user.Users;
import com.mesh_suite.dto.response.CustomFieldValueView;
import com.mesh_suite.dto.response.UserProfileView;
import com.mesh_suite.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class UsersMapper {

    private static final long PROFILE_IMAGE_ITEM_ID = 1L;

    private final CustomProfileValueRepository customProfileValueRepository;

    public UserResponse toUserResponse(Users user) {
        if (user == null || user.getId() == null) {
            return toUserResponse(user, List.of());
        }
        return toUserResponses(List.of(user)).get(0);
    }

    public List<UserResponse> toUserResponses(Collection<Users> users) {
        if (users == null || users.isEmpty()) {
            return List.of();
        }
        List<Long> ids = users.stream().map(Users::getId).filter(id -> id != null).toList();
        Map<Long, List<CustomProfileValue>> grouped = new HashMap<>();
        if (!ids.isEmpty()) {
            for (CustomProfileValue value : customProfileValueRepository
                    .findByOwnerTypeAndOwnerIdIn(CustomProfileOwnerType.USER, ids)) {
                grouped.computeIfAbsent(value.getOwnerId(), ignored -> new ArrayList<>()).add(value);
            }
        }
        return users.stream()
                .map(user -> toUserResponse(user, grouped.getOrDefault(user.getId(), List.of())))
                .toList();
    }

    private UserResponse toUserResponse(Users user, List<CustomProfileValue> storedValues) {
        if (user == null) {
            return null;
        }
        List<CustomFieldValueView> customValues = new ArrayList<>();
        boolean hasProfileImage = false;
        for (CustomProfileValue stored : storedValues) {
            customValues.add(new CustomFieldValueView(stored.getId(), stored.getItemId(), stored.getValue()));
            if (stored.getItemId() != null && stored.getItemId() == PROFILE_IMAGE_ITEM_ID) {
                hasProfileImage = true;
            }
        }
        if (!hasProfileImage && StringUtils.hasText(user.getProfileImage())) {
            customValues.add(new CustomFieldValueView(null, PROFILE_IMAGE_ITEM_ID, user.getProfileImage()));
        }

        Role role = user.getRole();
        List<UserProfileView> profiles = new ArrayList<>();
        if (role != null) {
            profiles.add(new UserProfileView(role.getId(), role.getRoleName()));
        }

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhoneNumber())
                .createdAt(user.getCreatedOn())
                .updatedAt(user.getUpdatedOn())
                .status(user.getStatus())
                .companyIdentifier(user.getCompanyIdentifier())
                .roleName(user.getRoleName())
                .roleId(role == null ? null : role.getId())
                .profileImage(user.getProfileImage())
                .customProfileValues(customValues)
                .profiles(profiles)
                .build();
    }
}
