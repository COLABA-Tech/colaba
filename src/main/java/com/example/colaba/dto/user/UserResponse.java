package com.example.colaba.dto.user;

public record UserResponse(
        Long id,
        String username,
        String email
) {
}
