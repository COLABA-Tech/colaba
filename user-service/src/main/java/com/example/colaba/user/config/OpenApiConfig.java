package com.example.colaba.user.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {

    @Value("${internal.api-key:}")
    private String internalApiKey;

    @Bean
    public OpenAPI customOpenAPI() {
        OpenAPI openAPI = new OpenAPI()
                .info(new Info()
                        .title("User Service API")
                        .version("1.0")
                        .description("API для управления пользователями"))
                .servers(List.of(
                        new Server()
                                .url("/")
                                .description("API Gateway")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new io.swagger.v3.oas.models.security.SecurityScheme()
                                .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Введите JWT-токен в формате: Bearer <token>")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));

        // Добавляем информацию об internal API key, если он есть
        if (internalApiKey != null && !internalApiKey.trim().isEmpty()) {
            String description = openAPI.getInfo().getDescription();
        }

        return openAPI;
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public-user-api")
                .pathsToMatch("/api/users/**")
                .pathsToExclude("/api/users/internal/**")
                .build();
    }

    @Bean
    public GroupedOpenApi internalApi() {
        return GroupedOpenApi.builder()
                .group("internal-user-api")
                .pathsToMatch("/api/users/internal/**")
                .addOperationCustomizer((operation, handlerMethod) -> {
                    operation.setSecurity(null);
                    return operation;
                })
                .build();
    }
}