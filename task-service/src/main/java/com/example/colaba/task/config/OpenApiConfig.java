package com.example.colaba.task.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
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
                .info(new Info()
                        .title("Task Service API")
                        .version("1.0")
                        .description("API для управления задачами и комментариями"))
                .servers(List.of(
                        new Server()
                                .url("/")
                                .description("API Gateway")
                ));
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
