package com.example.colaba.shared.webflux.client;

import com.example.colaba.shared.common.entity.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class UserServiceClient {
    private final WebClient webClient;

    public UserServiceClient(ReactorLoadBalancerExchangeFilterFunction lbFunction,
                             @Value("${internal.api-key}") String internalApiKey) {
        this.webClient = WebClient.builder()
                .filter(lbFunction)
                .defaultHeader("X-Internal-Key", internalApiKey)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    public Mono<Boolean> userExists(Long id) {
        return webClient.get()
                .uri("lb://user-service/api/users/internal/{id}/exists", id)
                .retrieve()
                .bodyToMono(Boolean.class);
    }

    public Mono<Boolean> isAdmin(Long id) {
        return webClient.get()
                .uri("lb://user-service/api/users/internal/{id}/is-admin", id)
                .retrieve()
                .bodyToMono(Boolean.class);
    }

    public Mono<UserRole> getUserRole(Long id) {
        return webClient.get()
                .uri("lb://user-service/api/users/internal/{id}/role", id)
                .retrieve()
                .bodyToMono(UserRole.class);
    }

    public Mono<Boolean> canManageUser(Long currentUserId, Long targetUserId) {
        return webClient.get()
                .uri("lb://user-service/api/users/internal/{currentUserId}/can-manage/{targetUserId}",
                        currentUserId, targetUserId)
                .retrieve()
                .bodyToMono(Boolean.class);
    }
}
