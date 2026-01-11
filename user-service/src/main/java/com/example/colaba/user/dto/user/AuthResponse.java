package com.example.colaba.user.dto.user;

public record AuthResponse(
        String token,
        UserResponse user
) {}
