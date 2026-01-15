package com.example.colaba.shared.common.exception.project;

import com.example.colaba.shared.common.exception.common.NotFoundException;

public class ProjectNotFoundException extends NotFoundException {

    public ProjectNotFoundException(Long id) {
        super("Project not found: ID " + id);
    }
}