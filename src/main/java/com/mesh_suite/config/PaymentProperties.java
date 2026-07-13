package com.mesh_suite.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "pay")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentProperties {
    @NotBlank
    private String clientSecret;

    @NotBlank
    private String clientId;

    @NotBlank
    private String baseUrl;

    @NotNull
    private Integer serviceId;
}
