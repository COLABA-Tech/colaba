package com.example.colaba.user.dto.user;

public record UserResponse(
        Long id,
        String username,
        String email,
        String role
) {
}
