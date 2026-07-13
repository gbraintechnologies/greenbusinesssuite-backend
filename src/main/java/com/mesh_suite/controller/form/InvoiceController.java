package com.mesh_suite.controller.form;

import com.mesh_suite.constant.forms.Timeline;
import com.mesh_suite.domain.form.Invoice;
import com.mesh_suite.dto.Paginate;
import com.mesh_suite.service.form.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mesh-suite/v1.0/invoices")
@Tag(name = "Invoice Management", description = "Endpoints for managing invoices")
public class InvoiceController {
    @Autowired
    private InvoiceService invoiceService;

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve an invoice by ID")
    public ResponseEntity<Invoice> getInvoiceById(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.getInvoiceById(id));
    }
    @GetMapping("/number/{invoiceNumber}")
    @Operation(summary = "Retrieve an invoice by ID")
    public ResponseEntity<Invoice> getByInvoiceNumber(@PathVariable String invoiceNumber) {
        return ResponseEntity.ok(invoiceService.getInvoiceByInvoiceNumber(invoiceNumber));
    }

    @GetMapping("/all/{page}/{size}/{timeline}")
    @Operation(summary = "Retrieve all invoices with pagination and timeline filtering")
    public ResponseEntity<Paginate<Invoice>> getAllInvoices(
            @PathVariable int page,
            @PathVariable int size,
            @PathVariable Timeline timeline) {
        
        Page<Invoice> invoicePage = invoiceService.getAllInvoices(page, size, timeline);
        Paginate<Invoice> response = new Paginate<>(
                invoicePage.getNumber(),
                invoicePage.getSize(),
                invoicePage.getTotalElements(),
                invoicePage.getTotalPages(),
                invoicePage.getContent()
        );
        return ResponseEntity.ok(response);
    }

/*
    @PutMapping("/update/{id}")
    @Operation(summary = "Update an invoice")
    public ResponseEntity<Invoice> updateInvoice(@PathVariable Long id, @RequestBody Invoice invoice) {
        return ResponseEntity.ok(invoiceService.updateInvoice(id, invoice));
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Delete an invoice")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/create")
    @Operation(summary = "Create a new invoice")
    public ResponseEntity<Long> createInvoice(@RequestBody Invoice invoice) {
        Invoice savedInvoice = invoiceService.createInvoice(invoice);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedInvoice.getId());
    }*/
}
