package com.example.colaba.project.circuit;

import com.example.colaba.project.client.UserServiceClient;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserServiceClientWrapper {

    private final UserServiceClient client;
    private final CircuitBreakerRegistry registry;

    private CircuitBreaker cb() {
        return registry.circuitBreaker("user-service");
    }

    public boolean userExists(Long userId) {
        return cb().executeSupplier(() -> client.userExists(userId));
    }
}