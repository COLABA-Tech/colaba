package com.example.colaba.task.config;

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
                        .title("Task Service API")
                        .version("1.0")
                        .description("API для управления задачами и комментариями"))
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
        }

        return openAPI;
    }

    @Bean
    public GroupedOpenApi publicTaskApi() {
        return GroupedOpenApi.builder()
                .group("public-task-api")
                .pathsToMatch("/api/tasks/**")
                .pathsToExclude("/api/tasks/internal/**")
                .build();
    }

    @Bean
    public GroupedOpenApi internalTaskApi() {
        return GroupedOpenApi.builder()
                .group("internal-task-api")
                .pathsToMatch("/api/tasks/internal/**")
                .addOperationCustomizer((operation, handlerMethod) -> {
                    operation.setSecurity(null);
                    return operation;
                })
                .build();
    }

    @Bean
    public GroupedOpenApi publicCommentApi() {
        return GroupedOpenApi.builder()
                .group("public-comment-api")
                .pathsToMatch("/api/comments/**")
                .build();
    }

    @Bean
    public GroupedOpenApi publicTagAssignmentApi() {
        return GroupedOpenApi.builder()
                .group("public-tag-assignment-api")
                .pathsToMatch("/api/task-tags/**")
                .build();
    }
}