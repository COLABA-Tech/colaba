package com.example.colaba.shared.dto.projectmember;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;

public record ProjectMemberResponse(
        Long projectId,
        String projectName,
        Long userId,
        String userUsername,
        String role,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        OffsetDateTime joinedAt
) {
}
