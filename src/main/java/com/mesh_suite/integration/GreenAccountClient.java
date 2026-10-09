package com.mesh_suite.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mesh_suite.domain.user.Users;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class GreenAccountClient {

    private static final String PRODUCT = "suite";

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String apiKey;

    public GreenAccountClient(
            ObjectMapper objectMapper,
            @Value("${green.account.url:}") String baseUrl,
            @Value("${green.account.api-key:}") String apiKey) {
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim().replaceAll("/$", "");
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));
        this.restTemplate = new RestTemplate(factory);
    }

    public boolean enabled() {
        return StringUtils.hasText(baseUrl) && StringUtils.hasText(apiKey);
    }

    public Optional<GreenAccountProfile> authenticate(String email, String password) {
        if (!enabled() || !StringUtils.hasText(email) || !StringUtils.hasText(password)) {
            return Optional.empty();
        }
        Map<String, String> body = new LinkedHashMap<>();
        body.put("email", email.trim());
        body.put("password", password);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/v1/accounts/authenticate",
                    new HttpEntity<>(body, headers()),
                    String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(parse(response.getBody()));
        } catch (RestClientException ex) {
            log.info("Green account did not authenticate {}: {}", email, ex.getMessage());
            return Optional.empty();
        }
    }

    public void provision(Users user) {
        if (!enabled() || user == null || user.getId() == null || !isBcrypt(user.getPassword())) {
            return;
        }
        String email = user.getEmail() == null ? "" : user.getEmail().trim();
        if (!StringUtils.hasText(email)) {
            return;
        }
        List<Map<String, Object>> identifiers = new ArrayList<>();
        identifiers.add(identifier("email", email, user.isVerified()));
        if (StringUtils.hasText(user.getPhoneNumber())) {
            identifiers.add(identifier("phone", user.getPhoneNumber().trim(), false));
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("product", PRODUCT);
        body.put("local_user_id", String.valueOf(user.getId()));
        body.put("name", displayName(user));
        body.put("password_hash", user.getPassword());
        body.put("identifiers", identifiers);
        try {
            restTemplate.postForEntity(
                    baseUrl + "/v1/accounts/provision",
                    new HttpEntity<>(body, headers()),
                    String.class);
        } catch (RestClientException ex) {
            log.warn("Green account provision failed for {}: {}", email, ex.getMessage());
        }
    }

    private GreenAccountProfile parse(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            String email = "";
            String phone = "";
            String ghanaCard = "";
            for (JsonNode item : root.path("identifiers")) {
                String kind = item.path("type").asText("");
                String value = item.path("value").asText("").trim();
                if (!StringUtils.hasText(value)) {
                    continue;
                }
                if ("email".equals(kind)) {
                    email = value;
                } else if ("phone".equals(kind) && !StringUtils.hasText(phone)) {
                    phone = value;
                } else if ("ghana_card".equals(kind) && !StringUtils.hasText(ghanaCard)) {
                    ghanaCard = value;
                }
            }
            if (!StringUtils.hasText(email)) {
                return null;
            }
            return GreenAccountProfile.builder()
                    .greenAccountId(root.path("green_account_id").asText(null))
                    .displayName(root.path("display_name").asText(null))
                    .email(email)
                    .phone(phone)
                    .ghanaCard(ghanaCard)
                    .build();
        } catch (Exception ex) {
            log.warn("Could not read Green account response: {}", ex.getMessage());
            return null;
        }
    }

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Api-Key", apiKey);
        return headers;
    }

    private static Map<String, Object> identifier(String type, String value, boolean verified) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("type", type);
        item.put("value", value);
        item.put("verified", verified);
        return item;
    }

    private static String displayName(Users user) {
        String first = user.getFirstName() == null ? "" : user.getFirstName().trim();
        String last = user.getLastName() == null ? "" : user.getLastName().trim();
        String name = (first + " " + last).trim();
        return StringUtils.hasText(name) ? name : user.getEmail();
    }

    private static boolean isBcrypt(String value) {
        return value != null && (value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$"));
    }
}
