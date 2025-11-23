package com.example.colaba.exception.projectmember;

import com.example.colaba.exception.common.NotFoundException;

public class ProjectMemberNotFoundException extends NotFoundException {
    public ProjectMemberNotFoundException(Long projectId, Long userId) {
        super("Project member not found: project ID " + projectId + ", user ID " + userId);
    }
}
