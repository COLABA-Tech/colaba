package com.example.colaba.auth.infrastructure.service;

import com.example.colaba.auth.application.dto.AuthResponse;
import com.example.colaba.auth.application.dto.LoginRequest;
import com.example.colaba.auth.application.dto.RegisterRequest;
import com.example.colaba.auth.application.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceFacade {
    private final AuthService authService;

    @Transactional
    public AuthResponse register(RegisterRequest registerRequest, Long currentUserId) {
        return authService.register(registerRequest, currentUserId);
    }

    public AuthResponse login(LoginRequest request) {
        return authService.login(request);
    }
}
