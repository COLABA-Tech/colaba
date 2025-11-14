package com.example.colaba.dto.projectmember;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record ProjectMemberResponse(
        Long projectId,
        String projectName,
        Long userId,
        String userUsername,
        String role,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime joinedAt
) {
}
