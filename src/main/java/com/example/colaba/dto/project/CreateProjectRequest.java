package com.example.colaba.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.Getter;
import org.springframework.web.bind.annotation.RestController;

@Data
@NoArgsConstructor
@AllArgsConstructor
public record CreateProjectRequest(
        @NotBlank String name,
        String description,
        @NotNull Long ownerId
) {}
