package com.mesh_suite.controller.user;

import com.mesh_suite.domain.user.Users;
import com.mesh_suite.dto.request.ChangePasswordRequest;
import com.mesh_suite.dto.request.UpdateUserCompanyDTO;
import com.mesh_suite.dto.request.UserFilterRequest;
import com.mesh_suite.dto.response.MessageResponse;
import com.mesh_suite.dto.response.UserResponse;
import com.mesh_suite.exception.UnAuthenticatedException;
import com.mesh_suite.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/mesh-suite/v1.0/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Operations related to managing users in the system")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public UserResponse getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        return userService.getUserByEmail(userDetails.getUsername(), userDetails);
    }

    @GetMapping("/authorities")
    @ResponseStatus(HttpStatus.OK)
    public UserDetails getCurrentUser1(@AuthenticationPrincipal UserDetails userDetails) {
        return userDetails;
    }

    @GetMapping("/me/permissions")
    @ResponseStatus(HttpStatus.OK)
    public List<String> getCurrentUserPermissions(@AuthenticationPrincipal Users user) {
        return user.getRole()
                .getPermissions()
                .stream()
                .map(permission -> permission.getName())
                .toList();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserResponse getUserById(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        return userService.getUserById(id, userDetails);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateUserStatus(@PathVariable Long id, @RequestParam boolean enabled, @AuthenticationPrincipal UserDetails userDetails) {
        userService.setUserEnabledStatus(id, enabled, userDetails);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        userService.deleteUser(id, userDetails);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserResponse> filterUsers(
            @RequestParam(required = false) List<String> roles,
            @RequestParam(required = false) List<Long> locationIds,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAfter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdBefore,
            @RequestParam(required = false) Boolean enabled,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UserFilterRequest request = new UserFilterRequest();
        request.setRoles(roles);
        request.setLocationIds(locationIds);
        request.setCreatedAfter(createdAfter);
        request.setCreatedBefore(createdBefore);
        request.setEnabled(enabled);

        return userService.filterUsers(request, userDetails);
    }

    @PutMapping("/{userId}/role/{roleId}")
    public ResponseEntity<MessageResponse> updateUserRole(@PathVariable Long userId, @PathVariable Long roleId, @AuthenticationPrincipal UserDetails userDetails) {
        MessageResponse response = userService.updateUserRole(userId, roleId, userDetails);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update user's company identifier")
    @PutMapping("/{id}/company-identifier")
    public ResponseEntity<UserResponse> updateCompanyIdentifier(
            @PathVariable Long id,
            @RequestBody @Valid UpdateUserCompanyDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.updateCompanyIdentifier(id, request, userDetails));
    }

    @PutMapping("/change-password")
    public ResponseEntity<MessageResponse> changePassword(
            @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        if (authentication == null) {
            throw new UnAuthenticatedException("Authentication required");
        }

        String email = authentication.getName();
        return ResponseEntity.ok(userService.changeUserPassword(email, request));
    }



}
