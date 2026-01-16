package com.example.colaba.shared.common.dto.user;

import lombok.Builder;

@Builder
public record UserResponse(
        Long id,
        String username,
        String email,
        String role
) {
}
