package com.example.colaba.project.service;

import com.example.colaba.shared.client.UserServiceClient;
import com.example.colaba.shared.entity.User;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserClientWrapper {

    private final UserServiceClient userServiceClient;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    private CircuitBreaker cb() {
        return circuitBreakerRegistry.circuitBreaker("userClient"); // имя из application.yml
    }

    // Этот метод — точная копия из shared, но с Circuit Breaker
    public User getUserEntityById(Long id) {
        return cb().executeSupplier(() -> userServiceClient.getUserEntityById(id));
    }
}