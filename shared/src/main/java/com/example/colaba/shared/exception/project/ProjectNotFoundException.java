package com.example.colaba.shared.exception.project;

import com.example.colaba.shared.exception.common.NotFoundException;

public class ProjectNotFoundException extends NotFoundException {

    public ProjectNotFoundException(Long id) {
        super("Project not found: ID " + id);
    }
}