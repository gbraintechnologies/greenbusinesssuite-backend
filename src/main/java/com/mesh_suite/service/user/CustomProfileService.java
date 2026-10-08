package com.mesh_suite.service.user;

import com.mesh_suite.dao.user.CustomProfileValueRepository;
import com.mesh_suite.domain.user.CustomProfileOwnerType;
import com.mesh_suite.domain.user.CustomProfileValue;
import com.mesh_suite.dto.request.CustomFieldInput;
import com.mesh_suite.dto.response.CustomFieldValueView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomProfileService {

    private final CustomProfileValueRepository customProfileValueRepository;

    @Transactional
    public void replace(CustomProfileOwnerType ownerType, Long ownerId, List<CustomFieldInput> fields) {
        if (ownerId == null || fields == null) {
            return;
        }
        for (CustomFieldInput field : fields) {
            if (field == null || field.getCustomProfileItemId() == null) {
                continue;
            }
            CustomProfileValue value = customProfileValueRepository
                    .findByOwnerTypeAndOwnerIdAndItemId(ownerType, ownerId, field.getCustomProfileItemId())
                    .orElseGet(() -> CustomProfileValue.builder()
                            .ownerType(ownerType)
                            .ownerId(ownerId)
                            .itemId(field.getCustomProfileItemId())
                            .build());
            value.setValue(field.getValue() == null ? "" : field.getValue());
            customProfileValueRepository.save(value);
        }
    }

    public Map<Long, List<CustomFieldValueView>> viewsFor(CustomProfileOwnerType ownerType, Collection<Long> ownerIds) {
        Map<Long, List<CustomFieldValueView>> grouped = new HashMap<>();
        if (ownerIds == null || ownerIds.isEmpty()) {
            return grouped;
        }
        for (CustomProfileValue stored : customProfileValueRepository.findByOwnerTypeAndOwnerIdIn(ownerType, ownerIds)) {
            grouped.computeIfAbsent(stored.getOwnerId(), ignored -> new ArrayList<>())
                    .add(new CustomFieldValueView(stored.getId(), stored.getItemId(), stored.getValue()));
        }
        return grouped;
    }

    public String valueFor(List<CustomFieldInput> fields, long itemId) {
        if (fields == null) {
            return null;
        }
        for (CustomFieldInput field : fields) {
            if (field != null && field.getCustomProfileItemId() != null && field.getCustomProfileItemId() == itemId) {
                return field.getValue();
            }
        }
        return null;
    }
}
