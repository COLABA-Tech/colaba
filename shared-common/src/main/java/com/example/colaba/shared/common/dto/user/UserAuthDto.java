package com.example.colaba.shared.common.dto.user;

public record UserAuthDto(
        Long id,
        String username,
        String email,
        String password,
        String role
) {
}
