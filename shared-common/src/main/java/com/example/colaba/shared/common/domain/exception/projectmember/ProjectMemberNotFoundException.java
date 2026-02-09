package com.example.colaba.shared.common.domain.exception.projectmember;

import com.example.colaba.shared.common.domain.exception.common.NotFoundException;

public class ProjectMemberNotFoundException extends NotFoundException {
    public ProjectMemberNotFoundException(Long projectId, Long userId) {
        super("Project member not found: project ID " + projectId + ", user ID " + userId);
    }
}
