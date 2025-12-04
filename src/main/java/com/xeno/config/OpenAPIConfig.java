package com.xeno.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI xenoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Xeno Shopify Insights API")
                        .description("Multi-tenant Shopify Data Ingestion & Insights Service - RESTful API for managing customer data, orders, and generating business insights.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Xeno Development Team")
                                .email("support@xeno.com")
                                .url("https://xeno-shopify-frontend-five.vercel.app"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("https://xeno-shopify-backend-frzt.onrender.com")
                                .description("Production Server (Render - Singapore)"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server")))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")
                                .description("Enter JWT Bearer token obtained from /api/auth/login")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }
}
