package com.mesh_suite.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "custom_profile_value",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_custom_profile_owner_item",
                columnNames = {"owner_type", "owner_id", "item_id"}
        )
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomProfileValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false, length = 20)
    private CustomProfileOwnerType ownerType;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(columnDefinition = "text")
    private String value;
}
