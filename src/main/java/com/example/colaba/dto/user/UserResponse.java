package com.example.colaba.dto.user;

import lombok.Getter;

@Getter
public record UserResponse(Long id, String username, String email) {
}
