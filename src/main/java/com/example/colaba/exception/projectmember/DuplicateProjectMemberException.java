package com.example.colaba.exception.projectmember;

import com.example.colaba.exception.common.DuplicateEntityException;

public class DuplicateProjectMemberException extends DuplicateEntityException {
    public DuplicateProjectMemberException(String username, Long projectId) {
        super("User '" + username + "' is already a member of project " + projectId);
    }
}
