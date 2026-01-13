package com.example.colaba.auth.dto;

import com.example.colaba.shared.common.dto.user.UserResponse;

public record AuthResponse(
        String token,
        UserResponse user
) {
}
