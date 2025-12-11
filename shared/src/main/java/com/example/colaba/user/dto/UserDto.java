package com.example.colaba.user.dto;  // Стандартный пакет для DTO

import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String email;
}