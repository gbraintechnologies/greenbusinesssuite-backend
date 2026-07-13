package com.mesh_suite.controller.form;

import com.mesh_suite.constant.forms.BillingType;
import com.mesh_suite.constant.forms.Status;
import com.mesh_suite.constant.forms.Timeline;
import com.mesh_suite.domain.form.Bill;
import com.mesh_suite.dto.Paginate;
import com.mesh_suite.exception.BillNotFoundException;
import com.mesh_suite.service.form.BillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Tag(name = "Bills API", description = "Endpoints for managing Bills")
@RestController
@RequestMapping("/mesh-suite/v1.0/bills")
public class BillController {

    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    @PostMapping
    @Operation(summary = "Create a Bill")
    public ResponseEntity<Long> createBill(@RequestBody Bill bill) {
        return ResponseEntity.ok(billService.createBill(bill));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Bill by ID")
    public ResponseEntity<Bill> getBillById(@PathVariable Long id) {
        return ResponseEntity.ok(billService.getBillById(id));
    }


    @Operation(summary = "Delete a Bill by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBill(@PathVariable Long id) {
        billService.deleteBill(id);
        return ResponseEntity.ok("Bill deleted successfully");
    }

    @PutMapping("/update-bill")
    @Operation(summary = "Modify a Bill")
    public ResponseEntity<Bill> updateBill(@RequestBody Bill bill) {
        return ResponseEntity.ok(billService.updateBill(bill));
    }

    @GetMapping("/all/{page}/{size}/{timeline}")
    @Operation(summary = "Retrieve all the Bill with Timeline")
    public ResponseEntity<Paginate<Bill>> getAllBills(
            @PathVariable int page,
            @PathVariable int size,
            @PathVariable Timeline timeline) {

        Page<Bill> billPage = billService.getAllBills(page, size, timeline);

        Paginate<Bill> response = new Paginate<>(
                billPage.getNumber(),
                billPage.getSize(),
                billPage.getTotalElements(),
                billPage.getTotalPages(),
                billPage.getContent()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/find-by/{serviceName}")
    @Operation(summary = "Find bills by service name (case insensitive).")
    public ResponseEntity<List<Bill>> getBillsByServiceName(@PathVariable String serviceName) {
        List<Bill> bills = billService.findBillByServiceName(serviceName);
        return ResponseEntity.ok(bills);
    }

    @GetMapping("/by-status/{status}")
    @Operation(summary = "Retrieve bills by status.")
    public ResponseEntity<List<Bill>> getBillsByStatus(@PathVariable Status status) {
            return ResponseEntity.ok(billService.findBillsByStatus(status));

    }
    @GetMapping("/by-type/{billingType}")
    @Operation(summary = "Retrieve bills with specific billing type { ONE_OFF_BILL, RECURRING_BILL }.")
    public List<Bill> getBillsByBillingType(@PathVariable BillingType billingType) {
        return billService.findByBillingType(billingType);
    }

    @GetMapping("/find-by-form_id/{formId}")
    @Operation(summary = "Retrieve bill by formId.")
    public ResponseEntity<Bill> getBillByFormId(@PathVariable Long formId) {
        Bill bill = billService.findBillByFormId(formId);
        return ResponseEntity.ok(bill);
    }

    @DeleteMapping("/delete-by-form_id/{formId}")
    @Operation(summary = "Delete bill by formId.")
    public ResponseEntity<?> deleteBillByFormId(@PathVariable Long formId) {
        try {
            billService.deleteBillByFormId(formId);
            return ResponseEntity.noContent().build();
        } catch (BillNotFoundException e) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Bill not found with form id "+formId);
        }
    }
}
