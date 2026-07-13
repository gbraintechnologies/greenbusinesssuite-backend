package com.mesh_suite.config;

import java.util.List;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .packagesToExclude("com.mesh_suite.exception")
                .pathsToMatch("/**")
                .build();
    }
   // @Bean
    public OpenAPI customOpenAPI() {
        Server httpsServer = new Server();
        httpsServer.setUrl("https://api-staging.meshsuites.com");
        httpsServer.setDescription("Production Server");

        return new OpenAPI().servers(List.of(httpsServer));
    }
}
