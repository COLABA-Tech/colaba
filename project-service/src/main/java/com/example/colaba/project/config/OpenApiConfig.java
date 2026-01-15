package com.example.colaba.project.config;

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
                        .title("Project Service API")
                        .version("1.0")
                        .description("API для управления проектами, участниками и тегами"))
                .servers(List.of(
                        new Server()
                                .url("/")
                                .description("API Gateway")
                ));
    }

    @Bean
    public GroupedOpenApi publicProjectApi() {
        return GroupedOpenApi.builder()
                .group("public-project-api")
                .pathsToMatch("/api/projects/**")
                .pathsToExclude("/api/projects/internal/**")
                .pathsToExclude("/api/projects/*/members/**")
                .build();
    }

    @Bean
    public GroupedOpenApi internalProjectApi() {
        return GroupedOpenApi.builder()
                .group("internal-project-api")
                .pathsToMatch("/api/projects/internal/**")
                .build();
    }

    @Bean
    public GroupedOpenApi publicProjectMembersApi() {
        return GroupedOpenApi.builder()
                .group("project-members-api")
                .pathsToMatch("/api/projects/*/members/**")
                .build();
    }

    @Bean
    public GroupedOpenApi publicTagApi() {
        return GroupedOpenApi.builder()
                .group("public-tag-api")
                .pathsToMatch("/api/tags/**")
                .pathsToExclude("/api/tags/internal/**")
                .build();
    }

    @Bean
    public GroupedOpenApi internalTagApi() {
        return GroupedOpenApi.builder()
                .group("internal-tag-api")
                .pathsToMatch("/api/tags/internal/**")
                .build();
    }
}

