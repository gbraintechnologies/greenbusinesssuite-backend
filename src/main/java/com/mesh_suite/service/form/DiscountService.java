package com.mesh_suite.service.form;

import com.mesh_suite.constant.forms.DiscountType;
import com.mesh_suite.constant.forms.Timeline;
import com.mesh_suite.dao.form.BillRepository;
import com.mesh_suite.dao.form.DiscountDataRepository;
import com.mesh_suite.dao.form.DiscountRepository;
import com.mesh_suite.domain.form.Bill;
import com.mesh_suite.domain.form.Discount;
import com.mesh_suite.domain.form.DiscountedData;
import com.mesh_suite.dto.DeactivationResponse;
import com.mesh_suite.exception.BillNotFoundException;
import com.mesh_suite.exception.DiscountNotFoundException;
import com.mesh_suite.util.TimelineFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class DiscountService {
    @Autowired
    private  DiscountRepository discountRepository;
    @Autowired
    private BillService billService;
    @Autowired
    private BillRepository billRepository;


    @Autowired
    private DiscountDataRepository discountDataRepository;

    public Long createDiscount(Discount discount){
        return discountRepository.save(discount).getId();
    }
    @Transactional(readOnly = true)
    public Page<Discount> getAllDiscounts(int page, int size, Timeline timeline) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdOn").descending());

        if (timeline == null || timeline == Timeline.ALL) {
            return discountRepository.findAll(pageable);
        }

        return discountRepository.findByCreatedOnAfter(
                TimelineFilter.calculateStartDate(timeline), pageable);
    }



    public Discount getDiscountById(Long id) {
        return discountRepository.findById(id)
                .orElseThrow(() -> new DiscountNotFoundException("Discount not found with id: " + id));
    }

    @Transactional
    public Long applyDiscountToBill(Long billId, Discount discount) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new BillNotFoundException("Bill not found"));

        // Fetch existing DiscountedData records for the bill
        List<DiscountedData> existingDiscounts = discountDataRepository.findByDiscountIdAndIsDeletedFalse(bill.getDiscountId());
        if (existingDiscounts != null && !existingDiscounts.isEmpty()) {
            for (DiscountedData existingDiscount : existingDiscounts) {
                existingDiscount.setIsDeleted(true); // Flag existing records as deleted
                discountDataRepository.save(existingDiscount);
            }
        }
        // Save the discount first to get its ID
        Discount savedDiscount = discountRepository.save(discount);

        // Compute data AFTER saving the discount
        DiscountedData discountedData = computeDiscountedData(bill, savedDiscount);

        // Update bill with the new discount ID
        bill.setDiscountId(savedDiscount.getId());
        billRepository.save(bill);

        // Save the discount data
        discountDataRepository.save(discountedData);

        return savedDiscount.getId();
    }
    @Transactional(readOnly = true)
    public List<Discount> findDiscountsByStatus(boolean isActive) {
        return discountRepository.findByIsActive(isActive);
    }

    @Transactional
    public Discount updateDiscount(Discount discountDetails) {
        Discount existingDiscount = getDiscountById(discountDetails.getId());
        existingDiscount.setDiscountType(discountDetails.getDiscountType());
        existingDiscount.setDiscountValue(discountDetails.getDiscountValue());
        existingDiscount.setIsActive(discountDetails.getIsActive());
        existingDiscount.setServiceName(discountDetails.getServiceName());
        return discountRepository.save(existingDiscount);
    }

    @Transactional
    public void deleteDiscount(Long id) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new DiscountNotFoundException("Discount not found with id: " + id));
        discountRepository.delete(discount);
    }

    public DiscountedData computeDiscountedData(Bill bill, Discount discount) {
        BigDecimal originalAmount = bill.getAmount();
        BigDecimal discountedPrice = billService.applyDiscount(originalAmount, discount);
        BigDecimal discountAmount = originalAmount.subtract(discountedPrice);
        BigDecimal discountPercentage = discount.getDiscountType() == DiscountType.PERCENTAGE
                ? discount.getDiscountValue()
                : BigDecimal.ZERO;

        return new DiscountedData(
                null,
                discount.getId(),
                bill.getServiceName(),
                originalAmount,
                discount.getDiscountType(),
                discountPercentage,
                discountAmount,
                discountedPrice,
                false,
                LocalDateTime.now()
        );
    }
    @Transactional
    public DeactivationResponse deactivateDiscount(Long id) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new DiscountNotFoundException(String.format("Discount with ID %d does not exist.", id)));

        if (!Boolean.TRUE.equals(discount.getIsActive())) {
            return new DeactivationResponse(false, String.format("Discount with ID %d is already inactive.", id));
        }

        discount.setIsActive(false);
        discountRepository.save(discount);

        return new DeactivationResponse(true, String.format("Discount with ID %d has been deactivated successfully.", id));
    }



}
