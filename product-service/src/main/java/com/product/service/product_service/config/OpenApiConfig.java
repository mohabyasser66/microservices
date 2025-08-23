package com.product.service.product_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI productServiceApi() {
        return new OpenAPI().info(new Info().title("Product Service API")
                .description("API documentation for Product Service")
                .version("1.0.0"))
                .externalDocs(new ExternalDocumentation()
                .description("Project Repository")
                .url("https://github.com/mohabyasser66/microservices"));
    }

}
