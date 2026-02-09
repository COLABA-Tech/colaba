package com.example.colaba.auth.infrastructure.controller;

import com.example.colaba.auth.application.dto.AuthResponse;
import com.example.colaba.auth.application.dto.LoginRequest;
import com.example.colaba.auth.application.dto.RegisterRequest;
import com.example.colaba.auth.infrastructure.service.AuthServiceFacade;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Auth Public", description = "API for authorization")
public class AuthController {

    private final AuthServiceFacade authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(authService.register(request, currentUserId));
    }
}
