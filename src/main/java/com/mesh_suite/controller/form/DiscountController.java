package com.mesh_suite.controller.form;

import com.mesh_suite.constant.forms.Timeline;
import com.mesh_suite.domain.form.Discount;
import com.mesh_suite.domain.form.DiscountedData;
import com.mesh_suite.dto.DeactivationResponse;
import com.mesh_suite.dto.Paginate;
import com.mesh_suite.service.form.DiscountService;
import com.mesh_suite.service.form.DiscountedDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Discount API", description = "Endpoints for managing discounts")
@RestController
@RequestMapping("/mesh-suite/v1.0/discounts")
public class DiscountController {
    @Autowired
    private  DiscountService discountService;

    @Autowired
    private DiscountedDataService discountedDataService;

    @Operation(summary = "Create new Discount")
    @GetMapping("/create")
    public ResponseEntity<Long> createDiscount(@RequestBody Discount discount) {
        return ResponseEntity.ok(discountService.createDiscount(discount));
    }
    @Operation(summary = "Retrieve all discounts with optional timeline filtering")
    @GetMapping("/all/{page}/{size}/{timeline}")
    public ResponseEntity<Paginate<Discount>> getAllDiscounts(
            @PathVariable int page,
            @PathVariable int size,
            @PathVariable Timeline timeline) {

        Page<Discount> discountPage = discountService.getAllDiscounts(page, size, timeline);

        Paginate<Discount> response = new Paginate<>(
                discountPage.getNumber(),
                discountPage.getSize(),
                discountPage.getTotalElements(),
                discountPage.getTotalPages(),
                discountPage.getContent()
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get discount by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Discount> getDiscountById(@PathVariable Long id) {
        return ResponseEntity.ok(discountService.getDiscountById(id));
    }

    @Operation(summary = "Apply Discount to a Bill")
    @PostMapping("/apply-discount/{billId}")
    public ResponseEntity<Long> addDiscount(@PathVariable Long billId,
                                            @RequestBody Discount discount) {
        return new ResponseEntity<>(discountService.applyDiscountToBill(billId,discount), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing discount")
    @PutMapping("/update")
    public ResponseEntity<Discount> updateDiscount(@RequestBody Discount discount) {
        return ResponseEntity.ok(discountService.updateDiscount(discount));
    }

    @Operation(summary = "Delete a discount by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDiscount(@PathVariable Long id) {
        discountService.deleteDiscount(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-status/{isActive}")
    @Operation(summary = "Retrieve discounts by status (true=active, false=inactive)")
    public ResponseEntity<List<Discount>> getDiscountsByStatus(@PathVariable boolean isActive) {
        return ResponseEntity.ok(discountService.findDiscountsByStatus(isActive));
    }


    @GetMapping("/service-data/{page}/{size}/{timeline}")
    @Operation(summary = "Retrieve all discounted service data with pagination and timeline filtering")
    public ResponseEntity<Paginate<DiscountedData>> getAllDiscountedData(
            @PathVariable int page,
            @PathVariable int size,
            @PathVariable Timeline timeline) {

        Page<DiscountedData> discountedDataPage = discountedDataService.getAllDiscountedData(page, size, timeline);
        Paginate<DiscountedData> response = new Paginate<>(
                discountedDataPage.getNumber(),
                discountedDataPage.getSize(),
                discountedDataPage.getTotalElements(),
                discountedDataPage.getTotalPages(),
                discountedDataPage.getContent()
        );
        return ResponseEntity.ok(response);
    }
    @GetMapping("/service-data/by-id/{id}")
    @Operation(summary = "Retrieve discounted service data by ID")
    public ResponseEntity<DiscountedData> getDiscountedDataById(@PathVariable Long id) {
        DiscountedData discountedData = discountedDataService.getDiscountedDataById(id);
        return ResponseEntity.ok(discountedData);
    }

    @PutMapping("/deactivate/{id}")
    @Operation(
            summary = "Deactivate a discount",
            description = "Deactivates the discount with the given ID if it is active."
    )
    public ResponseEntity<String> deactivateDiscount(@PathVariable Long id) {
        DeactivationResponse response = discountService.deactivateDiscount(id);
        HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response.getMessage());
    }




}
