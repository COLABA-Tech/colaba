package com.example.colaba.shared.dto.projectmember;

import com.example.colaba.shared.entity.projectmember.ProjectRole;

public record UpdateProjectMemberRequest(
        ProjectRole role
) {
}
