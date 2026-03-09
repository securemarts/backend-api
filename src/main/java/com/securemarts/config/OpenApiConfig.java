package com.securemarts.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("Securemarts Platform API")
                        .description("Multi-tenant commerce platform for Nigerian market")
                        .version("1.0.0"))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }

    /**
     * Default group: all endpoints. When any GroupedOpenApi is defined, springdoc does not
     * auto-create a "default" group, so we define it explicitly so the full API is visible.
     */
    @Bean
    public GroupedOpenApi allApis() {
        return GroupedOpenApi.builder()
                .group("All")
                .pathsToMatch("/**")
                .build();
    }

    /**
     * Dedicated Swagger group for Store customers and Invoicing so these endpoints
     * are easy to find in the Swagger UI dropdown (select "Merchant - Customers & Invoicing").
     */
    @Bean
    public GroupedOpenApi merchantCustomersAndInvoicingApi() {
        return GroupedOpenApi.builder()
                .group("Merchant - Customers & Invoicing")
                .pathsToMatch(
                        "/stores/*/customers",
                        "/stores/*/customers/**",
                        "/stores/*/invoices",
                        "/stores/*/invoices/**"
                )
                .build();
    }
}
