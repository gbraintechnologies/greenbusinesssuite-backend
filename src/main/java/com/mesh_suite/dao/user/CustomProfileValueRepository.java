package com.mesh_suite.dao.user;

import com.mesh_suite.domain.user.CustomProfileOwnerType;
import com.mesh_suite.domain.user.CustomProfileValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CustomProfileValueRepository extends JpaRepository<CustomProfileValue, Long> {

    List<CustomProfileValue> findByOwnerTypeAndOwnerId(CustomProfileOwnerType ownerType, Long ownerId);

    List<CustomProfileValue> findByOwnerTypeAndOwnerIdIn(CustomProfileOwnerType ownerType, Collection<Long> ownerIds);

    Optional<CustomProfileValue> findByOwnerTypeAndOwnerIdAndItemId(
            CustomProfileOwnerType ownerType,
            Long ownerId,
            Long itemId
    );
}
