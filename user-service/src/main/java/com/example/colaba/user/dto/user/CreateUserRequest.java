package com.example.colaba.user.dto.user;

import com.example.colaba.shared.common.entity.UserRole;
import jakarta.validation.constraints.*;

public record CreateUserRequest(
        @NotBlank
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username must contain only letters, numbers, and underscores")
        String username,

        @NotBlank
        @Email(message = "Email should be valid")
        @Size(max = 100, message = "Email must not exceed 100 characters")
        String email,

        @NotBlank
        @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$",
                message = "Password must contain minimum eight characters, at least one uppercase letter, one lowercase letter and one number"
        )
        String password,

        @NotNull(message = "Role cannot be null")
        UserRole role
) {
    public CreateUserRequest {
        if (role == null) role = UserRole.getDefault();
    }
}
