package com.example.colaba.dto.projectmember;

import com.example.colaba.entity.projectmember.ProjectRole;

public record UpdateProjectMemberRequest(
        ProjectRole role
) {
}
