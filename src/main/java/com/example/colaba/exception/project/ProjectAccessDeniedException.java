package com.example.colaba.exception.project;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ProjectAccessDeniedException extends RuntimeException {

    public ProjectAccessDeniedException(Long projectId, Long userId) {
        super("User " + userId + " has no access to project " + projectId);
    }

    public ProjectAccessDeniedException(String message) {
        super(message);
    }
}