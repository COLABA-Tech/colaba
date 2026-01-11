package com.example.colaba.project.dto.projectmember;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;

public record ProjectMemberResponse(
        Long projectId,
        Long userId,
        String role,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        OffsetDateTime joinedAt
) {
}
