package com.example.colaba.shared.dto.tag;

import jakarta.validation.constraints.Size;

public record UpdateTagRequest(
        @Size(max = 20, message = "Name must not exceed 20 characters")
        String name
) {
}
