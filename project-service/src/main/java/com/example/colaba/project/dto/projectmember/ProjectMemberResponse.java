package com.example.colaba.project.dto.projectmember;

public record ProjectMemberResponse(
        Long projectId,
        Long userId,
        String role
) {
}
