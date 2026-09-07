package com.mesh_suite.domain.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mesh_suite.constant.forms.UserStatus;
import com.mesh_suite.interceptor.TenantContext;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_tenant_username", columnNames = {"tenant_id", "username"}),
        @UniqueConstraint(name = "uk_users_tenant_email", columnNames = {"tenant_id", "email"})
})
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class Users implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(nullable = false)
    private String username;

    @Column(name = "company_identifier")
    private String companyIdentifier;

    @Column(name = "reset_code")
    private String resetCode;

    @Column(nullable = false)
    private String email;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @Column(nullable = false, name = "profile_image")
    private String profileImage;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(nullable = false)
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_on", updatable = false)
    private LocalDateTime createdOn;

    @UpdateTimestamp
    @Column(name = "updated_on")
    private LocalDateTime updatedOn;

    @Column(name = "role_name", nullable = false)
    private String roleName;

    @JoinColumn(name = "user_role")
    @ManyToOne(fetch = FetchType.EAGER)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Role role;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified = false;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<PasswordResetToken> passwordResetTokens = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<RefreshToken> refreshTokens = new ArrayList<>();

    @PreUpdate
    protected void onUpdate() {
        updatedOn = LocalDateTime.now();
    }

    @PrePersist
    private void assignTenant() {
        if (tenantId == null) {
            tenantId = TenantContext.getCurrentTenant();
        }
    }

    @Override
    @Transactional
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.getRoleName()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Transient
    public String getFullName() {
        return String.format("%s %s", firstName, lastName);
    }
}