package com.example.colaba.shared.common.dto.user;

public record UserResponse(
        Long id,
        String username,
        String email,
        String role
) {
}
