package com.mesh_suite.service.form;

import com.mesh_suite.constant.forms.Timeline;
import com.mesh_suite.dao.form.InvoiceRepository;
import com.mesh_suite.domain.form.Invoice;
import com.mesh_suite.exception.InvoiceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private BillService billService;
    @Transactional
    public Invoice createInvoice(Invoice invoice) {
        return invoiceRepository.save(invoice);
    }

    @Transactional(readOnly = true)
    public Invoice getInvoiceById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found"));
    }
    public Invoice getInvoiceByInvoiceNumber(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found with invoice number: " + invoiceNumber));
    }
    @Transactional(readOnly = true)
    public Page<Invoice> getAllInvoices(int page, int size, Timeline timeline) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdOn").descending());

        return (timeline == null || timeline == Timeline.ALL)
                ? invoiceRepository.findAll(pageable)
                : invoiceRepository.findByCreatedOnAfter(billService.calculateStartDate(timeline), pageable);
    }

}
