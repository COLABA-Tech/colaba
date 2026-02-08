package com.example.colaba.shared.common.domain.exception.project;

import com.example.colaba.shared.common.domain.exception.common.NotFoundException;

public class ProjectNotFoundException extends NotFoundException {

    public ProjectNotFoundException(Long id) {
        super("Project not found: ID " + id);
    }
}