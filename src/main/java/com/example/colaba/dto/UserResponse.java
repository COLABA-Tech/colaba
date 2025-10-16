package com.example.colaba.dto;

import com.example.colaba.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private UserRole role;
}

