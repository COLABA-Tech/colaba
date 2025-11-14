package com.example.colaba.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI colabaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("COLABA Task Management System API")
                        .description("REST API для системы управления задачами (аналог Jira/Trello)")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Development Team")
                                .email("team@colaba.example.com"))
                        .license(new License()
                                .name("Academic License")
                                .url("https://academic.example.com")));
    }
}