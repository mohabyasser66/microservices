package com.inventory.service.inventory_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI inventoryServiceApi() {
        return new OpenAPI().info(new Info().title("Inventory Service API")
                .description("API documentation for Inventory Service")
                .version("1.0.0"))
                .externalDocs(new ExternalDocumentation()
                .description("Project Repository")
                .url("https://github.com/mohabyasser66/microservices"));
    }

}
