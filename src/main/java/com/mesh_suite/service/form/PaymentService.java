package com.mesh_suite.service.form;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mesh_suite.constant.forms.*;
import com.mesh_suite.dao.form.InvoiceRepository;
import com.mesh_suite.dao.form.PaymentRepository;
import com.mesh_suite.domain.form.Bill;
import com.mesh_suite.domain.form.Invoice;
import com.mesh_suite.domain.form.Payment;
import com.mesh_suite.dto.PaymentDto;
import com.mesh_suite.dto.PaymentResponse;
import com.mesh_suite.dto.TransactionStatusResponse;
import com.mesh_suite.dto.request.PaymentRequest;
import com.mesh_suite.exception.PaymentGatewayException;
import com.mesh_suite.exception.PaymentNotFoundException;
import com.mesh_suite.exception.PaymentValidationException;
import com.mesh_suite.interceptor.TenantContext;
import com.mesh_suite.util.PaymentGatewayClient;
import com.mesh_suite.util.UniqueIdGenerator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

@Service
@Slf4j
public class PaymentService {

    @Value("${pay.service-id}")
    private Integer serviceId;

    private final PaymentRepository paymentRepository;
    private final BillService billService;
    private final InvoiceRepository invoiceRepository;
    private final PaymentGatewayClient paymentGatewayClient;
    private final ObjectMapper mapper;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository,
                          BillService billService,
                          InvoiceRepository invoiceRepository,
                          PaymentGatewayClient paymentGatewayClient,
                          ObjectMapper mapper){
        this.paymentRepository = paymentRepository;
        this.billService = billService;
        this.invoiceRepository = invoiceRepository;
        this.paymentGatewayClient = paymentGatewayClient;
        this.mapper = mapper;
    }


    public PaymentResultDto makePayment(PaymentDto paymentDto, HttpServletRequest request) {
        log.info("=== START PROCESSING PAYMENT === BillId={}, transRef={}",
                paymentDto.getBillId(), paymentDto.getTransRef());

        String tenantId = getTenantId(request);
        TenantContext.setCurrentTenant(tenantId);

        Payment payment = null;

        try {

            Bill bill = billService.getBillById(paymentDto.getBillId());
            if (bill == null) {
                throw new PaymentValidationException("Bill not found");
            }
            payment = resolvePayment(paymentDto, bill.getAmount());
            payment.setAmountPaid(bill.getAmount());


            // Guard: never reprocess a terminal payment
            if (payment.getId() != null && isTerminal(payment.getStatus())) {
                throw new PaymentValidationException(
                        "Payment already in terminal state: " + payment.getStatus());
            }


            // If retrying an existing payment that already has a transRef,
            // ask the gateway what actually happened before resubmitting
            if (payment.getId() != null && StringUtils.hasText(payment.getTransRef())) {
                payment = reconcileWithGateway(payment);
                if (isTerminal(payment.getStatus())) {
                    return buildResultFromTerminalPayment(payment);
                }
            }

            // Generate transRef only when there isn't one
            if (!StringUtils.hasText(payment.getTransRef())) {
                payment.setTransRef(UniqueIdGenerator.generateTransRef());
            }

            // Orchard rejects exttrid > 20 chars with resp_code 025
            if (payment.getTransRef().length() > 20) {
                throw new PaymentValidationException(
                        "transRef exceeds 20-character gateway limit: "
                                + payment.getTransRef());
            }


            validatePayment(payment);

            payment.setStatus(PaymentStatus.PENDING);
            payment = paymentRepository.save(payment);

            // Build and submit to gateway
            PaymentRequest gatewayRequest = buildPaymentRequest(payment, tenantId);
            HttpResponse<String> httpResponse =
                    paymentGatewayClient.processPayment(gatewayRequest);

            return processGatewayResponse(httpResponse, payment);

        } catch (PaymentGatewayException e) {
            log.error("GATEWAY ERROR txId={}", payment != null ? payment.getTransRef() : "N/A", e);
            return handleSystemError(payment, e);

        } catch (Exception e) {
            log.error("UNEXPECTED ERROR txId={}", payment != null ? payment.getTransRef() : "N/A", e);
            return handleSystemError(payment, e);

        } finally {
            TenantContext.clear();
            log.info("=== END PROCESSING PAYMENT ===");
        }
    }


    @Transactional
    public void processPaymentCallback(TransactionStatusResponse callback, String tenantid) {

        TenantContext.setCurrentTenant(tenantid);

        log.info("===> Processing Payment Callback | tenantid={}", tenantid);

        if (!StringUtils.hasText(callback.getTransRef()) &&
                !StringUtils.hasText(callback.getTransId())) {
            throw new IllegalArgumentException(
                    "Callback must contain either trans_ref or trans_id");
        }

        // Try transRef first, fall back to transId
        Payment payment = null;

        if (StringUtils.hasText(callback.getTransRef())) {
            payment = paymentRepository.findByTransRefForUpdate(callback.getTransRef())
                    .orElse(null);
        }
        if (payment == null && StringUtils.hasText(callback.getTransId())) {
            payment = paymentRepository.findByTransactionId(callback.getTransId())
                    .orElse(null);
        }
        if (payment == null) {
            throw new PaymentNotFoundException(
                    "Payment not found for trans_ref=" + callback.getTransRef()
                            + ", trans_id=" + callback.getTransId());
        }

        PaymentStatus incomingStatus = determinePaymentStatus(callback.getTransStatus());

        log.info("Payment [{}] | Current={} | Incoming={}",
                payment.getId(), payment.getStatus(), incomingStatus);

        // Ignore callbacks for payments already in a terminal state
        if (isTerminal(payment.getStatus())) {
            log.info("Ignoring callback. Payment already terminal: {}", payment.getStatus());
            return;
        }

        payment.setStatus(incomingStatus);

        if (StringUtils.hasText(callback.getTransId())) {
            payment.setTransactionId(callback.getTransId());
        }

        if (incomingStatus == PaymentStatus.SUCCESSFUL) {
            payment.setDatePaid(LocalDateTime.now());
            if (!invoiceRepository.existsByBillId(payment.getBillId())) {
                createInvoice(payment);
            }
        }

        paymentRepository.save(payment);

        log.info("Payment [{}] updated successfully to {}", payment.getId(), incomingStatus);
    }


    @Transactional(readOnly = true)
    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id "+id));
    }
    @Transactional(readOnly = true)
    public Page<Payment> getAllPayments(int page, int size, Timeline timeline) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("datePaid").descending());

        return (timeline == null || timeline == Timeline.ALL)
                ? paymentRepository.findAll(pageable)
                : paymentRepository.findByDatePaidAfter(billService.calculateStartDate(timeline), pageable);
    }


    public List<Payment> getPaymentsByMethod(PaymentMethod paymentMethod) {
        return paymentRepository.findByPaymentMethod(paymentMethod);
    }
    public BigDecimal getTotalRevenue() {
        BigDecimal totalRevenue = paymentRepository.findTotalRevenue();
        return totalRevenue != null ? totalRevenue.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    public BigDecimal getTotalRevenueWithinTimeline(Timeline timeline) {
        BigDecimal totalRevenue = (timeline == Timeline.ALL) ? getTotalRevenue()
                : paymentRepository.findTotalRevenueWithinDateRange(billService.calculateStartDate(timeline), LocalDateTime.now());
        return totalRevenue != null ? totalRevenue.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }
    public List<Payment> getPaymentsByServiceName(String serviceName) {
        return paymentRepository.findByServiceNameContainingIgnoreCase(serviceName);
    }

    public List<Payment> getPaymentsByCustomerName(String customerName) {
        return paymentRepository.findByCustomerNameContainingIgnoreCase(customerName);
    }


    // ═══════════════════════════════════════════════════════════════════════════
    // Private — payment flow helpers
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Parses and persists the synchronous response from the Orchard gateway.
     *
     * resp_code mapping per Orchard API docs:
     *   015 — Request received for processing → PENDING  (normal happy path)
     *   027 — Request successfully completed  → PENDING  (await callback)
     *   002 — Pending                         → PENDING
     *   000 — Successful                      → SUCCESSFUL
     *   001, 003 — Business failure           → FAILED
     *   021 — Duplicate exttrid               → FAILED
     *   026 — Incomplete parameters           → FAILED
     *   028 — Invalid amount                  → FAILED
     *   default                               → PENDING (let callback resolve)
     */
    private PaymentResultDto processGatewayResponse(
            HttpResponse<String> httpResponse, Payment payment) {

        PaymentResponse response = null;

        try {
            if (httpResponse.body() != null && !httpResponse.body().isEmpty()) {
                response = mapper.readValue(httpResponse.body(), PaymentResponse.class);
            }
        } catch (Exception e) {
            log.error("Failed to parse gateway response: {}", httpResponse.body(), e);
        }

        String respCode = response != null ? response.getRespCode() : "SYSTEM_ERROR";
        String respDesc = response != null ? response.getRespDesc()
                : "Payment is being processed. You will be notified shortly.";

        PaymentStatus newStatus = switch (respCode) {
            case "015", "027", "002" -> PaymentStatus.PENDING;
            case "000"               -> PaymentStatus.SUCCESSFUL;
            case "001", "003",
                 "021", "026", "028" -> PaymentStatus.FAILED;
            default -> {
                log.warn("Unrecognised resp_code='{}' for transRef={} — defaulting to PENDING",
                        respCode, payment.getTransRef());
                yield PaymentStatus.PENDING;
            }
        };

        payment.setStatus(newStatus);
        paymentRepository.save(payment);

        log.info("Gateway response processed | transRef={} | status={} | respCode={} | respDesc={}",
                payment.getTransRef(), newStatus, respCode, respDesc);

        return new PaymentResultDto(
                payment.getId(),
                newStatus,
                respCode,
                respDesc,
                payment.getTransRef(),
                payment.getPaymentMethod()
        );
    }

    /**
     * Queries the gateway for the true state of a transRef before retrying.
     *
     * Decision table:
     *   null (033)  → no gateway record  → keep transRef, proceed to resubmit
     *   SUCCESSFUL  → already processed  → fix DB, surface as terminal
     *   PENDING     → still in flight    → block retry, prevent duplicate
     *   FAILED      → confirmed failed   → clear transRef, allow fresh submit
     *
     * Blocks on uncertainty: if the TSC call itself fails, we throw rather
     * than allow a retry that might result in a double charge.
     */
    private Payment reconcileWithGateway(Payment payment) {
        try {
            log.info("Reconciling transRef={} with gateway before retry",
                    payment.getTransRef());

            TransactionStatusResponse status =
                    paymentGatewayClient.checkTransactionStatus(payment.getTransRef());

            if (status == null) {
                // 033 — gateway has no record, safe to resubmit with same transRef
                log.info("No gateway record for transRef={} — safe to resubmit",
                        payment.getTransRef());
                return payment;
            }

            PaymentStatus gatewayStatus = determinePaymentStatus(status.getTransStatus());

            log.info("Reconciliation result | transRef={} | gatewayStatus={}",
                    payment.getTransRef(), gatewayStatus);

            switch (gatewayStatus) {
                case SUCCESSFUL -> {
                    log.warn("Reconciliation found SUCCESSFUL transRef={} — correcting DB",
                            payment.getTransRef());
                    payment.setStatus(PaymentStatus.SUCCESSFUL);
                    payment.setDatePaid(LocalDateTime.now());
                    if (!invoiceRepository.existsByBillId(payment.getBillId())) {
                        createInvoice(payment);
                    }
                    paymentRepository.save(payment);
                }
                case PENDING -> {
                    log.info("Gateway shows PENDING for transRef={} — blocking retry",
                            payment.getTransRef());
                    payment.setStatus(PaymentStatus.PENDING);
                    paymentRepository.save(payment);
                }
                case FAILED -> {
                    // Confirmed failed at gateway — clear transRef so a new one
                    // is generated and the payment can be cleanly resubmitted
                    log.info("Gateway confirmed FAILED for transRef={} — clearing for resubmit",
                            payment.getTransRef());
                    payment.setTransRef(null);
                    payment.setStatus(PaymentStatus.FAILED);
                }
            }

        } catch (PaymentGatewayException e) {
            // TSC itself failed — block the retry rather than risk a double charge
            log.error("Gateway status check failed for transRef={} — blocking retry",
                    payment.getTransRef(), e);
            throw new PaymentGatewayException(
                    "Could not verify transaction status before retry. " +
                            "Please try again later or contact support.");
        }

        return payment;
    }


    private PaymentResultDto buildResultFromTerminalPayment(Payment payment) {
        return new PaymentResultDto(
                payment.getId(),
                payment.getStatus(),
                payment.getStatus() == PaymentStatus.SUCCESSFUL ? "000" : "001",
                payment.getStatus() == PaymentStatus.SUCCESSFUL
                        ? "Payment already completed"
                        : "Payment previously failed",
                payment.getTransRef(),
                payment.getPaymentMethod()
        );
    }


    // ═══════════════════════════════════════════════════════════════════════════
    // Private — helper methods
    // ═══════════════════════════════════════════════════════════════════════════

    private String getTenantId(HttpServletRequest request) {
        String tenantId = request.getHeader("tenantid");
        if (!StringUtils.hasText(tenantId)) {
            throw new PaymentValidationException("Tenant ID is required in headers");
        }
        return tenantId;
    }

    private void validatePayment(Payment payment) {
        log.info("Validating payment: Trans_Ref={}, amount={}, method={}, network={}",
                payment.getTransRef(), payment.getAmountPaid(),
                payment.getPaymentMethod(), payment.getNetwork());

        // Validate payment method
        if (payment.getPaymentMethod() == null) {
            log.error("Payment validation failed: Payment method is missing");
            throw new PaymentValidationException("Payment method is required");
        }

        // Validate network
        if (payment.getNetwork() == null) {
            log.error("Payment validation failed: Network is missing");
            throw new PaymentValidationException("Network is required");
        }

        // Validate amount
        if (payment.getAmountPaid() == null ||
                payment.getAmountPaid().compareTo(BigDecimal.ZERO) <= 0) {
            log.error("Payment validation failed: Invalid amount {}", payment.getAmountPaid());
            throw new PaymentValidationException("Valid payment amount is required");
        }

        switch (payment.getPaymentMethod()) {
            case MOBILE_MONEY:
                // Phone number is required for mobile money
                if (!StringUtils.hasText(payment.getPhoneNumber())) {
                    log.error("Payment validation failed: Phone number missing for mobile money");
                    throw new PaymentValidationException(
                            "Phone number is required for mobile money payments");
                }

                if (!EnumSet.of(Network.MTN, Network.VOD, Network.AIR, Network.TIG)
                        .contains(payment.getNetwork())) {
                    log.error("Payment validation failed: Invalid network {} for mobile money",
                            payment.getNetwork());
                    throw new PaymentValidationException(
                            "Invalid network for mobile money payment");
                }

                // Normalize MSISDN for gateway
                payment.setPhoneNumber(normalizeMsisdn(payment.getPhoneNumber()));
                break;

            case CREDIT_DEBIT_CARD:
                // Optional phone number: normalize if provided
                if (StringUtils.hasText(payment.getPhoneNumber())) {
                    payment.setPhoneNumber(normalizeMsisdn(payment.getPhoneNumber()));
                }

                if (!EnumSet.of(Network.VIS, Network.MAS).contains(payment.getNetwork())) {
                    log.error("Payment validation failed: Invalid network {} for card payment",
                            payment.getNetwork());
                    throw new PaymentValidationException("Invalid network for card payment");
                }
                break;

            case BANK_TRANSFER:
                // Optional phone number: normalize if provided
                if (StringUtils.hasText(payment.getPhoneNumber())) {
                    payment.setPhoneNumber(normalizeMsisdn(payment.getPhoneNumber()));
                }

                if (payment.getNetwork() != Network.BNK) {
                    log.error("Payment validation failed: Network {} must be BNK for bank transfer",
                            payment.getNetwork());
                    throw new PaymentValidationException("Network must be BNK for bank payments");
                }

                if (!StringUtils.hasText(payment.getBankCode())) {
                    log.error("Payment validation failed: Bank code missing for bank transfer");
                    throw new PaymentValidationException(
                            "Bank code is required for bank payments");
                }
                break;

            default:
                throw new PaymentValidationException(
                        "Unsupported payment method: " + payment.getPaymentMethod());
        }

        log.info("Payment validation passed for transRef={}", payment.getTransRef());
    }

    private PaymentRequest buildPaymentRequest(Payment payment, String tenantid) {
        PaymentRequest request = new PaymentRequest();

        request.setCustomerNumber(normalizeMsisdn(payment.getPhoneNumber()));
        request.setAmount(payment.getAmountPaid().setScale(2, RoundingMode.HALF_UP));
        request.setExttrid(payment.getTransRef());
        request.setReference(generateReference(payment.getServiceName()));
        request.setNw(mapNetwork(payment.getNetwork()));
        request.setTransType("CTM");
        request.setCallbackUrl(paymentGatewayClient.buildCallbackUrl(tenantid));
        request.setServiceId(serviceId);
        request.setTs(paymentGatewayClient.getCurrentTimestamp());
        request.setRecipientName(payment.getCustomerName());
        request.setNickname(generateNickname(payment.getServiceName()));

        if (payment.getNetwork() == Network.BNK) {
            request.setBankCode(payment.getBankCode());
        }

        log.info("Callback URL sent to gateway: {}", request.getCallbackUrl());

        return request;
    }

    private void createInvoice(Payment payment) {
        Invoice invoice = new Invoice();
        invoice.setBillId(payment.getBillId());
        invoice.setInvoiceNumber(UniqueIdGenerator.generateInvoiceId(payment.getBillId()));
        invoice.setCustomerName(payment.getCustomerName());
        invoice.setCustomerEmail(payment.getCustomerEmail());
        invoice.setServiceName(payment.getServiceName());
        invoice.setAmount(payment.getAmountPaid());
        invoiceRepository.save(invoice);
    }

    private PaymentResultDto handleSystemError(Payment payment, Exception ex) {
        if (ex != null) {
            log.error("System error during payment processing", ex);
        }
        if (payment != null) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
        }
        return new PaymentResultDto(
                payment != null ? payment.getId() : null,
                PaymentStatus.FAILED,
                "SYSTEM_ERROR",
                "An error occurred while processing your payment",
                payment != null ? payment.getTransRef() : null,
                payment != null ? payment.getPaymentMethod() : null
        );
    }

    private Payment resolvePayment(PaymentDto dto, BigDecimal amount) {
        if (dto.getResponseId() != null) {

            return paymentRepository.findByResponseId(dto.getResponseId())
                    .orElse(new Payment(dto, amount));
        }
        return new Payment(dto, amount);
    }

    private String mapNetwork(Network network) {
        return switch (network) {
            case MTN      -> "MTN";
            case VOD      -> "VOD";
            case AIR, TIG -> "AIR";
            case MAS      -> "MAS";
            case VIS      -> "VIS";
            case BNK      -> "BNK";
            default -> throw new PaymentValidationException(
                    "Unsupported network: " + network);
        };
    }

    private String normalizeMsisdn(String msisdn) {
        if (!StringUtils.hasText(msisdn)) {
            return msisdn;
        }

        String cleaned = msisdn.trim();

        if (cleaned.startsWith("+")) {
            cleaned = cleaned.substring(1);
        }

        cleaned = cleaned.replaceAll("\\D", "");

        // 0542878621 → 233542878621
        if (cleaned.length() == 10 && cleaned.startsWith("0")) {
            cleaned = "233" + cleaned.substring(1);
        }

        // Already correct format
        if (cleaned.length() == 12 && cleaned.startsWith("233")) {
            return cleaned;
        }

        log.warn("Unexpected MSISDN format after normalization: {} (original: {})",
                cleaned, msisdn);

        return cleaned;
    }

    private String generateReference(String serviceName) {
        String ref = ("P" + serviceName)
                .replaceAll("[^A-Za-z0-9]", "")
                .toUpperCase();
        return ref.length() > 10 ? ref.substring(0, 10) : ref;
    }

    private String generateNickname(String serviceName) {
        String nick = "Pay " + serviceName;
        return nick.length() > 15 ? nick.substring(0, 15) : nick;
    }
    private boolean isTerminal(PaymentStatus status) {
        return status == PaymentStatus.SUCCESSFUL
                || status == PaymentStatus.FAILED
                || status == PaymentStatus.CANCELLED;
    }

    private PaymentStatus determinePaymentStatus(String transStatus) {

        if (!StringUtils.hasText(transStatus)) {
            return PaymentStatus.FAILED;
        }

        // Orchard uses "/" as separator
        String statusCode = transStatus.split("/")[0].trim();

        return switch (statusCode) {
            case "000" -> PaymentStatus.SUCCESSFUL;
            case "002" -> PaymentStatus.PENDING;
            case "001", "003" -> PaymentStatus.FAILED;
            default -> {
                log.warn("Unknown trans_status from gateway: {}", transStatus);
                yield PaymentStatus.FAILED;
            }
        };
    }


}
