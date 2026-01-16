package com.example.colaba.shared.common.config;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.List;

@Configuration
public class SwaggerInternalConfig {

    @Value("${internal.api-key:tvulOBWkyfz+TfDMFKWxiZxsBXy8ODfzqX+4TnNSQD+Z+ihYaNS4n2j+1ios3rRM}")
    private String internalApiKey;

    @Bean
    public OperationCustomizer internalApiKeyCustomizer() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            if (handlerMethod == null || internalApiKey == null || internalApiKey.isEmpty()) {
                return operation;
            }

            if (isInternalEndpoint(handlerMethod, operation)) {
                configureInternalOperation(operation);
            }

            return operation;
        };
    }

    private boolean isInternalEndpoint(HandlerMethod handlerMethod, Operation operation) {
        Method method = handlerMethod.getMethod();

        String path = extractPathFromAnnotations(method, handlerMethod.getBeanType());
        if (path != null && path.contains("/internal/")) {
            return true;
        }

        if (method.getName().toLowerCase().contains("internal")) {
            return true;
        }

        if (operation != null && operation.getOperationId() != null) {
            if (operation.getOperationId().toLowerCase().contains("internal")) {
                return true;
            }
        }

        if (operation != null && operation.getDescription() != null) {
            if (operation.getDescription().toLowerCase().contains("internal")) {
                return true;
            }
        }

        return false;
    }

    private String extractPathFromAnnotations(Method method, Class<?> controllerClass) {
        String classPath = "";
        if (controllerClass.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping classMapping = controllerClass.getAnnotation(RequestMapping.class);
            if (classMapping.value().length > 0) {
                classPath = classMapping.value()[0];
            }
        }

        String methodPath = "";

        if (method.isAnnotationPresent(GetMapping.class)) {
            GetMapping mapping = method.getAnnotation(GetMapping.class);
            if (mapping.value().length > 0) {
                methodPath = mapping.value()[0];
            }
        } else if (method.isAnnotationPresent(PostMapping.class)) {
            PostMapping mapping = method.getAnnotation(PostMapping.class);
            if (mapping.value().length > 0) {
                methodPath = mapping.value()[0];
            }
        } else if (method.isAnnotationPresent(PutMapping.class)) {
            PutMapping mapping = method.getAnnotation(PutMapping.class);
            if (mapping.value().length > 0) {
                methodPath = mapping.value()[0];
            }
        } else if (method.isAnnotationPresent(DeleteMapping.class)) {
            DeleteMapping mapping = method.getAnnotation(DeleteMapping.class);
            if (mapping.value().length > 0) {
                methodPath = mapping.value()[0];
            }
        } else if (method.isAnnotationPresent(PatchMapping.class)) {
            PatchMapping mapping = method.getAnnotation(PatchMapping.class);
            if (mapping.value().length > 0) {
                methodPath = mapping.value()[0];
            }
        } else if (method.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping mapping = method.getAnnotation(RequestMapping.class);
            if (mapping.value().length > 0) {
                methodPath = mapping.value()[0];
            }
        }

        return normalizePath(classPath + methodPath);
    }

    private String normalizePath(String path) {
        if (path == null) return "";
        return path.replace("//", "/").replace("//", "/");
    }

    private void configureInternalOperation(Operation operation) {
        operation.setSecurity(null);

        addInternalApiKeyParameter(operation);

        addInternalDescription(operation);

        addInternalTag(operation);
    }

    private void addInternalApiKeyParameter(Operation operation) {
        Parameter apiKeyParam = new Parameter()
                .in("header")
                .name("X-Internal-Key")
                .description("Internal API Key для межсервисного взаимодействия")
                .required(true)
                .example(internalApiKey)
                .schema(new io.swagger.v3.oas.models.media.Schema<>()
                        .type("string")
                        .example(internalApiKey));

        if (operation.getParameters() == null) {
            operation.setParameters(List.of(apiKeyParam));
        } else {
            boolean exists = operation.getParameters().stream()
                    .anyMatch(p -> "X-Internal-Key".equals(p.getName()));

            if (!exists) {
                operation.getParameters().add(0, apiKeyParam);
            }
        }
    }

    private void addInternalDescription(Operation operation) {
        String originalDesc = operation.getDescription() != null ? operation.getDescription() : "";

        if (!originalDesc.contains("Internal Endpoint")) {
            String internalDesc = "\n\n**🔒 Internal Endpoint**\n" +
                    "• Требует заголовок: `X-Internal-Key`\n" +
                    "• Используется для межсервисного взаимодействия\n" +
                    "• API Key автоматически подставляется в Swagger UI";

            operation.setDescription(originalDesc + internalDesc);
        }
    }

    private void addInternalTag(Operation operation) {
        if (operation.getTags() != null) {
            boolean hasInternalTag = operation.getTags().stream()
                    .anyMatch(tag -> "Internal".equals(tag) || "internal".equalsIgnoreCase(tag));

            if (!hasInternalTag) {
                operation.addTagsItem("Internal");
            }
        } else {
            operation.addTagsItem("Internal");
        }
    }
}