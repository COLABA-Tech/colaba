package com.example.colaba.project.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server()
                                .url("/")  // ОТНОСИТЕЛЬНЫЙ ПУТЬ!
                                .description("API Gateway")
                ));
    }

    // Опционально: явная группа для API
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("project-service")
                .pathsToMatch("/api/**")
                .build();
    }
}

