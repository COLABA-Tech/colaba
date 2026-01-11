package com.example.colaba.shared.exception.projectmember;

import com.example.colaba.shared.exception.common.DuplicateEntityException;

public class DuplicateProjectMemberException extends DuplicateEntityException {
    public DuplicateProjectMemberException(Long userId, Long projectId) {
        super("User " + userId + " is already a member of project " + projectId);
    }
}
