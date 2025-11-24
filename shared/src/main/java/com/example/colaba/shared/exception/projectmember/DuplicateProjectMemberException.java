package com.example.colaba.shared.exception.projectmember;

import com.example.colaba.shared.exception.common.DuplicateEntityException;

public class DuplicateProjectMemberException extends DuplicateEntityException {
    public DuplicateProjectMemberException(String username, Long projectId) {
        super("User '" + username + "' is already a member of project " + projectId);
    }
}
