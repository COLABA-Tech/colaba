package com.example.colaba.user.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class TaskServiceClient {

    private final WebClient webClient;

    public TaskServiceClient(ReactorLoadBalancerExchangeFilterFunction lbFunction,
                             @Value("${internal.api-key}") String internalApiKey) {
        this.webClient = WebClient.builder()
                .filter(lbFunction)
                .defaultHeader("X-Internal-Key", internalApiKey)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    public Mono<Void> deleteTasksByProject(Long projectId) {
        return webClient.delete()
                .uri("lb://task-service/api/tasks/internal/project/{projectId}", projectId)
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Mono<Void> handleUserDeletion(Long userId) {
        return webClient.post()
                .uri("lb://task-service/api/tasks/internal/user/{userId}/deletion", userId)
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Mono<Boolean> taskExists(Long id) {
        return webClient.get()
                .uri("lb://task-service/api/tasks/internal/{id}/exists", id)
                .retrieve()
                .bodyToMono(Boolean.class);
    }

    public Mono<Void> deleteTaskTagsByTagId(Long tagId) {
        return webClient.delete()
                .uri("lb://task-service/api/tasks/internal/task-tags/tag/{tagId}", tagId)
                .retrieve()
                .bodyToMono(Void.class);
    }
}
