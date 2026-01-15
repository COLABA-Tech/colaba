package com.example.colaba.shared.common.exception.projectmember;

import com.example.colaba.shared.common.exception.common.DuplicateEntityException;

public class DuplicateProjectMemberException extends DuplicateEntityException {
    public DuplicateProjectMemberException(Long userId, Long projectId) {
        super("User " + userId + " is already a member of project " + projectId);
    }
}
