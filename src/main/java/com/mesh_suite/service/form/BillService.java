package com.mesh_suite.service.form;

import com.mesh_suite.constant.forms.BillingType;
import com.mesh_suite.constant.forms.Status;
import com.mesh_suite.constant.forms.Timeline;
import com.mesh_suite.dao.form.BillRepository;
import com.mesh_suite.dao.form.DiscountRepository;
import com.mesh_suite.dao.form.InvoiceRepository;
import com.mesh_suite.domain.form.Bill;
import com.mesh_suite.domain.form.Discount;
import com.mesh_suite.exception.BillNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@Transactional
@Slf4j
public class BillService {
    @Autowired
    private  BillRepository billRepository;
    @Autowired
    private  DiscountRepository discountRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    public Long createBill(Bill bill) {
        if (bill.getPaymentMethod() == null) {
            throw new BillNotFoundException("Payment methods cannot be empty.");
        }
        // Check if a bill already exists for the given formId
        boolean formIdExists = billRepository.existsByFormId(bill.getFormId());
        if (formIdExists) {
            throw new IllegalArgumentException(String.format("A bill already exists for the provided formId: %d", bill.getFormId()));
        }

        return billRepository.save(bill).getId();
    }

    @Transactional(readOnly = true)
    public Bill getBillById(Long billId) {
        log.info("Fetching bill with ID: {}", billId);

        return billRepository.findById(billId)
            .map(bill -> {
                log.info("Bill found: {}", bill);
                Bill discountedBill = applyActiveDiscount(bill);
                log.info("Applied active discount. Final bill amount: {}", discountedBill.getAmount());
                return discountedBill;
            })
            .orElseThrow(() -> {
                log.error("Bill not found with ID: {}", billId);
                return new BillNotFoundException("Bill not found with ID: " + billId);
            });
    }

    public Bill updateBill(Bill bill) {
        return billRepository.findById(bill.getId())
                .map(existingBill -> {
                    copyBillProperties(existingBill, bill);
                    return billRepository.save(existingBill);
                })
                .orElseThrow(() -> new BillNotFoundException("Bill not found with ID: " + bill.getId()));
    }

    public void deleteBill(Long id) {
        if (!billRepository.existsById(id)) {
            throw new BillNotFoundException("Bill not found with ID: " + id);
        }
        billRepository.deleteById(id);
    }


    @Transactional(readOnly = true)
    public Page<Bill> getAllBills(int page, int size, Timeline timeline) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdOn").descending());
        Page<Bill> bills = (timeline == null || timeline == Timeline.ALL)
                ? billRepository.findAll(pageable)
                : billRepository.findByCreatedOnAfter(calculateStartDate(timeline), pageable);

        bills.forEach(this::applyActiveDiscount);
        return bills;
    }

    @Transactional(readOnly = true)
    public List<Bill> findBillByServiceName(String serviceName) {
        return applyActiveDiscountToList(billRepository.findByServiceNameContainingIgnoreCase(serviceName.trim()));
    }

    @Transactional(readOnly = true)
    public List<Bill> findBillsByStatus(Status status) {
        return applyActiveDiscountToList(billRepository.findByStatus(status));
    }

    @Transactional(readOnly = true)
    public List<Bill> findByBillingType(BillingType type) {
        return applyActiveDiscountToList(billRepository.findByBillingType(type));
    }

    @Transactional(readOnly = true)
    public Bill findBillByFormId(Long formId) {
        return billRepository.findByFormId(formId)
                .map(this::applyActiveDiscount)
                .orElseThrow(() -> new BillNotFoundException("Bill not found with formId: " + formId));
    }


    @Transactional
    public void deleteBillByFormId(Long formId) {
        List<Bill> bills = billRepository.findAllByFormId(formId);
        if (bills.isEmpty()) {
            log.warn("No bill found for formId: {}", formId);
            throw new BillNotFoundException("Bill not found with formId: " + formId);
        }
        billRepository.delete(bills.get(0));
        log.info("Bill deleted for formId: {}", formId);
    }


    // ========================  Utility Methods =========================
    public void copyBillProperties(Bill existingBill, Bill newBill) {
        existingBill.setBillingType(newBill.getBillingType());
        existingBill.setServiceName(newBill.getServiceName());
        existingBill.setCurrency(newBill.getCurrency());
        existingBill.setAmount(newBill.getAmount());
        existingBill.setFrequency(newBill.getFrequency());
        existingBill.setPaymentMethod(newBill.getPaymentMethod());
        existingBill.setStatus(newBill.getStatus());
        existingBill.setDiscountId(newBill.getDiscountId());
    }

     public Bill applyActiveDiscount(Bill bill) {
        if (bill.getDiscountId() != null && bill.getDiscountId() > 0) {
            discountRepository.findByIdAndIsActiveTrue(bill.getDiscountId())
                    .ifPresent(discount -> bill.setAmount(applyDiscount(bill.getAmount(), discount)));
        }
        return bill;
    }

    public List<Bill> applyActiveDiscountToList(List<Bill> bills) {
        bills.forEach(this::applyActiveDiscount);
        return bills;
    }

    public BigDecimal applyDiscount(BigDecimal amount, Discount discount) {
        return switch (discount.getDiscountType()) {
            case PERCENTAGE -> amount.subtract(amount.multiply(discount.getDiscountValue().divide(BigDecimal.valueOf(100))));
            case AMOUNT -> amount.subtract(discount.getDiscountValue());
        };
    }

    public LocalDateTime calculateStartDate(Timeline timeline) {
        LocalDateTime now = LocalDateTime.now();
        return switch (timeline) {
            case TODAY -> now.toLocalDate().atStartOfDay();
            case THIS_WEEK -> now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case THIS_MONTH -> now.with(TemporalAdjusters.firstDayOfMonth());
            case THIS_YEAR -> now.with(TemporalAdjusters.firstDayOfYear());
            default -> throw new IllegalArgumentException("Invalid timeline");
        };
    }
}
