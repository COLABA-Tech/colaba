package com.example.colaba.shared.dto.projectmember;

import com.example.colaba.project.entity.projectmember.ProjectRole;

public record UpdateProjectMemberRequest(
        ProjectRole role
) {
}
