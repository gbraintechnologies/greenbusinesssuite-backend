package com.mesh_suite.service.form;

import com.mesh_suite.constant.forms.Timeline;
import com.mesh_suite.dao.form.DiscountedDataRepository;
import com.mesh_suite.domain.form.DiscountedData;
import com.mesh_suite.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DiscountedDataService {
    @Autowired
    private DiscountedDataRepository discountedDataRepository;
    @Autowired
    private BillService billService;


    @Transactional(readOnly = true)
    public DiscountedData getDiscountedDataById(Long id) {
        return discountedDataRepository.findById(id)
                .map(discountedData -> {
                    if (Boolean.TRUE.equals(discountedData.getIsDeleted())) {
                        throw new IllegalStateException(
                                String.format("Discount Service data with ID %d is marked as deleted.", id)
                        );
                    }
                    return discountedData;
                })
                .orElseThrow(() -> new ResourceNotFoundException("DiscountedData not found for ID: " + id));
    }

    @Transactional(readOnly = true)
    public Page<DiscountedData> getAllDiscountedData(int page, int size, Timeline timeline) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdOn").descending());

        Page<DiscountedData> discountedDataPage = (timeline == null || timeline == Timeline.ALL)
                ? discountedDataRepository.findByIsDeletedFalse(pageable)
                : discountedDataRepository.findByIsDeletedFalseAndCreatedOnAfter(billService.calculateStartDate(timeline), pageable);


        return discountedDataPage;
    }

}
