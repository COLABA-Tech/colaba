package com.example.colaba.shared.webflux.client;

import com.example.colaba.shared.common.dto.project.ProjectResponse;
import com.example.colaba.shared.common.dto.tag.TagResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class ProjectServiceClient {

    private final WebClient webClient;

    public ProjectServiceClient(ReactorLoadBalancerExchangeFilterFunction lbFunction,
                                @Value("${internal.api-key}") String internalApiKey) {
        this.webClient = WebClient.builder()
                .filter(lbFunction)
                .defaultHeader("X-Internal-Key", internalApiKey)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    public Mono<Boolean> hasAnyRole(Long projectId, Long userId) {
        return webClient.get()
                .uri("lb://project-service/api/projects/internal/{projectId}/user/{userId}/any-role", projectId, userId)
                .retrieve()
                .bodyToMono(Boolean.class);
    }

    public Mono<Boolean> isAtLeastEditor(Long projectId, Long userId) {
        return webClient.get()
                .uri("lb://project-service/api/projects/internal/{projectId}/user/{userId}/at-least-editor", projectId, userId)
                .retrieve()
                .bodyToMono(Boolean.class);
    }

    public Mono<Boolean> isOwner(Long projectId, Long userId) {
        return webClient.get()
                .uri("lb://project-service/api/projects/internal/{projectId}/user/{userId}/owner", projectId, userId)
                .retrieve()
                .bodyToMono(Boolean.class);
    }

    public Mono<String> getUserProjectRole(Long projectId, Long userId) {
        return webClient.get()
                .uri("lb://project-service/api/projects/internal/{projectId}/user/{userId}/role", projectId, userId)
                .retrieve()
                .bodyToMono(String.class);
    }
}
