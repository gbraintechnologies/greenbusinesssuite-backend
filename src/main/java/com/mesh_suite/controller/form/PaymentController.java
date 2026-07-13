package com.mesh_suite.controller.form;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mesh_suite.constant.forms.PaymentMethod;
import com.mesh_suite.constant.forms.PaymentResultDto;
import com.mesh_suite.constant.forms.Timeline;
import com.mesh_suite.constant.shared.AppConstants;
import com.mesh_suite.domain.form.Payment;
import com.mesh_suite.dto.Paginate;
import com.mesh_suite.dto.PaymentDto;
import com.mesh_suite.dto.TransactionStatusResponse;
import com.mesh_suite.exception.PaymentNotFoundException;
import com.mesh_suite.interceptor.TenantContext;
import com.mesh_suite.service.form.PaymentService;
import com.mesh_suite.util.PaymentGatewayClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/mesh-suite/v1.0/payments")
@Tag(name = "Payments", description = "Endpoints for managing payment operations")
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService,
                             PaymentGatewayClient paymentGatewayClient,
                             ObjectMapper mapper) {
        this.paymentService = paymentService;
    }

    @Operation(summary = "Make a new payment")
    @PostMapping
    public PaymentResultDto createPayment(
            @Valid @RequestBody PaymentDto payment,
            HttpServletRequest request) {

        return paymentService.makePayment(payment, request);
    }
    @Operation(summary = "Retrieve a payment by its ID")
    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }
    @GetMapping("/all/{page}/{size}/{timeline}")
    @Operation(summary = "Retrieve all payments with timeline filter")
    public ResponseEntity<Paginate<Payment>> getAllPayments(
            @PathVariable int page,
            @PathVariable int size,
            @PathVariable Timeline timeline) {

        Page<Payment> paymentPage = paymentService.getAllPayments(page, size, timeline);

        Paginate<Payment> response = new Paginate<>(
                paymentPage.getNumber(),
                paymentPage.getSize(),
                paymentPage.getTotalElements(),
                paymentPage.getTotalPages(),
                paymentPage.getContent()
        );

        return ResponseEntity.ok(response);
    }
    @Operation(summary = "Retrieve payments by payment method")
    @GetMapping("/method/{paymentMethod}")
    public ResponseEntity<List<Payment>> getPaymentsByMethod(
            @PathVariable PaymentMethod paymentMethod) {
        return ResponseEntity.ok(paymentService.getPaymentsByMethod(paymentMethod));
    }
    @Operation(summary = "Retrieve total revenue from all payments")
    @GetMapping("/revenue")
    public ResponseEntity<BigDecimal> getTotalRevenue() {
        return ResponseEntity.ok(paymentService.getTotalRevenue());
    }

    @Operation(summary = "Retrieve total revenue filtered by timeline")
    @GetMapping("/revenue/{timeline}")
    public ResponseEntity<BigDecimal> getTotalRevenueWithinTimeline(
            @PathVariable Timeline timeline) {
        return ResponseEntity.ok(paymentService.getTotalRevenueWithinTimeline(timeline));
    }

    @Operation(summary = "Retrieve payments by service name")
    @GetMapping("/service/{serviceName}")
    public ResponseEntity<List<Payment>> getPaymentsByServiceName(@PathVariable String serviceName) {
        return ResponseEntity.ok(paymentService.getPaymentsByServiceName(serviceName));
    }


    @Operation(summary = "Search payments by customer name")
    @GetMapping("/customer/{customerName}")
    public ResponseEntity<List<Payment>> getPaymentsByCustomerName(@PathVariable String customerName) {
        List<Payment> payments = paymentService.getPaymentsByCustomerName(customerName);
        return ResponseEntity.ok(payments);
    }

    @PostMapping("/trigger/callback")
    public ResponseEntity<String> handlePaymentCallback(
            @RequestBody TransactionStatusResponse callbackResponse,
            @RequestParam(AppConstants.TENANT_ID_PARAM) String tenantid) {

        log.info("Received payment callback | tenantid={} | payload={}", tenantid, callbackResponse);

        if (!StringUtils.hasText(tenantid)) {
            log.error("Callback rejected: missing tenantid");
            return ResponseEntity.badRequest().body("Missing tenantid");
        }


        try {

            paymentService.processPaymentCallback(callbackResponse, tenantid);

            log.info("Callback processed successfully | trans_ref={} | trans_id={}",
                    callbackResponse.getTransRef(),
                    callbackResponse.getTransId()
            );

            return ResponseEntity.ok("OK");

        } catch (PaymentNotFoundException ex) {

            log.error("Payment not found | trans_ref={} | trans_id={}",
                    callbackResponse.getTransRef(),
                    callbackResponse.getTransId(),
                    ex
            );

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Payment record not found for reference");

        } catch (IllegalArgumentException ex) {
            log.error("Invalid callback data", ex);
            return ResponseEntity.badRequest().body(ex.getMessage());

        } catch (Exception ex) {

            log.error("Unexpected error during callback processing", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process callback");

        } finally {

            TenantContext.clear();
            log.debug("Tenant context cleared for tenantid={}", tenantid);
        }
    }

}
