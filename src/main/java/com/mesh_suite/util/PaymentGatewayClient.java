package com.mesh_suite.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mesh_suite.constant.shared.AppConstants;
import com.mesh_suite.dto.TransactionStatusResponse;
import com.mesh_suite.dto.request.PaymentRequest;
import com.mesh_suite.dto.request.TransactionStatusRequest;
import com.mesh_suite.exception.PaymentGatewayException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;

@Slf4j
@Component
public class PaymentGatewayClient {

    private static final String SEND_REQUEST_PATH = "/sendRequest";
    private static final String CHECK_TX_PATH     = "/checkTransaction";
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    @Value("${pay.client-secret:}")
    private String clientSecret;

    @Value("${pay.client-id:}")
    private String clientId;

    @Value("${pay.base-url:}")
    private String baseUrl;

    @Value("${pay.service-id}")
    private Integer serviceId;

    @Value("${pay.callback-base-url}")
    private String callbackBaseUrl;

    private final ObjectMapper mapper;

    @Autowired
    public PaymentGatewayClient(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public HttpResponse<String> processPayment(PaymentRequest paymentRequest)
            throws PaymentGatewayException {
        try {

            String jsonString    = mapper.writeValueAsString(paymentRequest);
            String authorization = buildAuthorizationHeader(jsonString);

            log.debug("Payment request payload prepared and signed");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + SEND_REQUEST_PATH))
                    .header("Authorization", authorization)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                    .build();

            HttpResponse<String> response =
                    HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            log.info("Gateway response received | status={}", response.statusCode());
            return response;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PaymentGatewayException("Payment processing interrupted: " + e);
        } catch (Exception e) {
            log.error("Error processing payment request", e);
            throw new PaymentGatewayException("Failed to process payment request: " + e);
        }
    }

    /**
     * Queries the Orchard gateway for the true status of a previously
     * submitted transaction. Used before retrying a payment to prevent
     * duplicate charges.
     *
     * Returns null if the gateway has no record of the exttrid (resp_code 033).
     * Throws PaymentGatewayException on network failure or rejected request.
     */
    public TransactionStatusResponse checkTransactionStatus(String exttrid)
            throws PaymentGatewayException {
        try {
            TransactionStatusRequest statusRequest = new TransactionStatusRequest();
            statusRequest.setExttrid(exttrid);
            statusRequest.setServiceId(serviceId);


            String jsonString    = mapper.writeValueAsString(statusRequest);
            String authorization = buildAuthorizationHeader(jsonString);

            log.info("Checking transaction status | exttrid={}", exttrid);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + CHECK_TX_PATH))
                    .header("Authorization", authorization)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                    .build();

            HttpResponse<String> response =
                    HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            log.info("TSC response received | httpStatus={} | exttrid={}",
                    response.statusCode(), exttrid);

            return parseTransactionStatusResponse(response.body(), exttrid);

        } catch (PaymentGatewayException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PaymentGatewayException(
                    "Transaction status check interrupted | exttrid=" + exttrid);
        } catch (Exception e) {
            log.error("Unexpected error during TSC | exttrid={}", exttrid, e);
            throw new PaymentGatewayException(
                    "Failed to check transaction status | exttrid=" + exttrid + ": " + e);
        }
    }

    private String buildAuthorizationHeader(String jsonPayload) throws Exception {
        String signature = getSignature(jsonPayload);
        return clientId + ":" + signature;
    }

    public String getSignature(String jsonPayload) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hmac = Mac.getInstance("HmacSHA256");

        SecretKeySpec secretKey = new SecretKeySpec(
                clientSecret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
        hmac.init(secretKey);
        return HexFormat.of()
                .formatHex(hmac.doFinal(jsonPayload.getBytes(StandardCharsets.UTF_8)));
    }

    public String getCurrentTimestamp() {
        return ZonedDateTime.now(ZoneId.of("UTC")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public String buildCallbackUrl(String tenantid) {
        return UriComponentsBuilder
                .fromHttpUrl(callbackBaseUrl)
                .path("/mesh-suite/v1.0/payments/trigger/callback")
                .queryParam(AppConstants.TENANT_ID_PARAM, tenantid)
                .build()
                .toUriString();
    }


    /**
     * Parses the TSC response body. Two shapes are possible from Orchard:
     *
     *   Error shape:   { "resp_code": "033", "resp_desc": "..." }
     *   Success shape: { "trans_status": "000/01", "trans_ref": "...", ... }
     *
     * resp_code 033 = transaction not found → return null (safe to resubmit).
     * Any other resp_code = request-level rejection (auth, params) → throw.
     */
    private TransactionStatusResponse parseTransactionStatusResponse(
            String body, String exttrid) throws PaymentGatewayException {

        if (body == null || body.isBlank()) {
            log.warn("Empty TSC response body | exttrid={}", exttrid);
            return null;
        }

        try {
            JsonNode root = mapper.readTree(body);

            if (root.has("resp_code")) {
                String respCode = root.get("resp_code").asText();
                String respDesc = root.has("resp_desc")
                        ? root.get("resp_desc").asText() : "No description";

                if ("033".equals(respCode)) {
                    log.info("Gateway has no record of exttrid={} (033) — safe to resubmit",
                            exttrid);
                    return null;
                }

                log.error("TSC rejected | exttrid={} | code={} | desc={}",
                        exttrid, respCode, respDesc);
                throw new PaymentGatewayException(
                        "Transaction status check rejected: [" + respCode + "] " + respDesc);
            }

            TransactionStatusResponse result =
                    mapper.treeToValue(root, TransactionStatusResponse.class);

            log.info("TSC parsed | exttrid={} | trans_status={} | message={}",
                    exttrid, result.getTransStatus(), result.getMessage());

            return result;

        } catch (PaymentGatewayException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse TSC response | exttrid={} | body={}", exttrid, body, e);
            throw new PaymentGatewayException(
                    "Failed to parse transaction status response | exttrid=" + exttrid);
        }
    }
}