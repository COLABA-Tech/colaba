package com.example.colaba.shared.dto.user;

public record UserResponse(
        Long id,
        String username,
        String email
) {
}
