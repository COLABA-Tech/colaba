package com.example.colaba.project.dto.projectmember;

import com.example.colaba.shared.common.entity.ProjectRole;

public record UpdateProjectMemberRequest(
        ProjectRole role
) {
}
