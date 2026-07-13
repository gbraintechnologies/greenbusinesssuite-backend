package com.mesh_suite.config;

import java.util.List;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                title = "Mesh Business Suite API",
                version = "1.0",
                description = "This API documentation is for Mesh Business Suite, designed for Logiciel Ltd",
                contact = @Contact(
                        name = "Email Developer",
                        email = "gatidavid2012@gmail.com"
                ),
                license = @License(
                        name = "Mesh, All Rights Reserved",
                        url = "https://logicielghana.com/about-us"))
)
public class OpenAPIConfiguration {

}
