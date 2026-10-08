package com.mesh_suite.service.company;

import com.mesh_suite.domain.user.CustomProfileOwnerType;
import com.mesh_suite.dto.response.CompanyResponseDTO;
import com.mesh_suite.dto.response.CustomFieldValueView;
import com.mesh_suite.service.user.CustomProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CompanyResponseEnricher {

    private static final long DESCRIPTION_ITEM_ID = 1L;

    private final CustomProfileService customProfileService;

    public CompanyResponseDTO enrich(CompanyResponseDTO company) {
        if (company == null) {
            return null;
        }
        enrich(List.of(company));
        return company;
    }

    public void enrich(List<CompanyResponseDTO> companies) {
        if (companies == null || companies.isEmpty()) {
            return;
        }
        List<Long> ids = companies.stream().map(CompanyResponseDTO::getId).filter(id -> id != null).toList();
        Map<Long, List<CustomFieldValueView>> grouped = customProfileService.viewsFor(CustomProfileOwnerType.COMPANY, ids);
        for (CompanyResponseDTO company : companies) {
            List<CustomFieldValueView> values = new ArrayList<>(grouped.getOrDefault(company.getId(), List.of()));
            boolean hasDescription = values.stream()
                    .anyMatch(value -> value.getCustomProfileItemId() != null
                            && value.getCustomProfileItemId() == DESCRIPTION_ITEM_ID);
            if (!hasDescription && StringUtils.hasText(company.getDescription())) {
                values.add(new CustomFieldValueView(null, DESCRIPTION_ITEM_ID, company.getDescription()));
            }
            company.setCompanyCustomValues(values);
        }
    }
}
