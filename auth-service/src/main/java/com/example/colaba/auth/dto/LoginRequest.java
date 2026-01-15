package com.example.colaba.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Login (username or email) is required")
        String login,

        @NotBlank(message = "Password is required")
        String password
) {
}
