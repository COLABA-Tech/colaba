package com.example.colaba.shared.common.application.security;

import io.jsonwebtoken.Claims;

public interface JwtTokenService {
    String generateToken(Long userId, String role);

    public Claims validateToken(String token);

    Long extractId(String token);

    String extractRole(String token);
}