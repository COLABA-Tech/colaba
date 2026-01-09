package com.example.colaba.project.dto.projectmember;

import com.example.colaba.project.entity.projectmember.ProjectRole;

public record UpdateProjectMemberRequest(
        ProjectRole role
) {
}
