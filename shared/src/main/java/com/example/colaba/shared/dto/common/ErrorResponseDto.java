package com.example.colaba.shared.dto.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponseDto {
    private String error;
    private int code;
    private String message;
}
