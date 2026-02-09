package com.example.colaba.project.dto.projectmember;

import com.example.colaba.shared.common.domain.entity.ProjectRole;

public record UpdateProjectMemberRequest(
        ProjectRole role
) {
}
