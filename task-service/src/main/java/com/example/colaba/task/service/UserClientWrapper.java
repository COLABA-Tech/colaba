package com.example.colaba.task.service;

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
    private final CircuitBreakerRegistry registry;

    private CircuitBreaker cb() {
        return registry.circuitBreaker("userClient");
    }

    public User getUserEntityById(Long id) {
        return cb().executeSupplier(() -> userServiceClient.getUserEntityById(id));
    }
}