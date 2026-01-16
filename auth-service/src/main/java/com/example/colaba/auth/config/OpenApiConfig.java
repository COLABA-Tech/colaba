package com.example.colaba.auth.config;

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
                        .title("Auth Service API")
                        .version("1.0")
                        .description("API для аутентификации и авторизации"))
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

        if (internalApiKey != null && !internalApiKey.trim().isEmpty()) {
            String description = openAPI.getInfo().getDescription();
            openAPI.getInfo().setDescription(description +
                    "\n\n**Internal Endpoints**\n" +
                    "Для вызова internal эндпоинтов (`/auth/internal/**`) требуется заголовок:\n" +
                    "`X-Internal-Key: " + internalApiKey + "`\n" +
                    "Этот заголовок автоматически подставляется в Swagger UI.");
        }

        return openAPI;
    }

    @Bean
    public GroupedOpenApi publicAuthApi() {
        return GroupedOpenApi.builder()
                .group("public-auth-api")
                .pathsToMatch("/auth/**")
                .pathsToExclude("/auth/internal/**")
                .build();
    }

    @Bean
    public GroupedOpenApi internalAuthApi() {
        return GroupedOpenApi.builder()
                .group("internal-auth-api")
                .pathsToMatch("/auth/internal/**")
                .addOperationCustomizer((operation, handlerMethod) -> {
                    operation.setSecurity(null);
                    return operation;
                })
                .build();
    }
}