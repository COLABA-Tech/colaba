package com.example.colaba.shared.webmvc.circuit;

import com.example.colaba.shared.common.dto.user.UserAuthDto;
import com.example.colaba.shared.webmvc.client.UserServiceClient;
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

    public UserAuthDto findForAuthByUsername(String username) {
        return cb().executeSupplier(() -> client.findForAuthByUsername(username));
    }

    public UserAuthDto findForAuthByEmail(String email) {
        return cb().executeSupplier(() -> client.findForAuthByEmail(email));
    }

    public UserAuthDto createUser(UserAuthDto userAuthDto) {
        return cb().executeSupplier(() -> client.createUser(userAuthDto));
    }
}