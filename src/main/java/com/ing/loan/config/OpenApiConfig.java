package com.ing.loan.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Loan API",
        version = "1.0",
        description = "API for managing loans for customers"
    ),
    servers = @Server(url = "http://localhost:8090", description = "Local server")
)
public class OpenApiConfig {
}
// http://localhost:8090/swagger-ui/index.html
// http://localhost:8090/v3/api-docs