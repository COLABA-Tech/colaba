package com.example.colaba.exception.project;

import com.example.colaba.exception.common.NotFoundException;

public class ProjectNotFoundException extends NotFoundException {

    public ProjectNotFoundException(Long id) {
        super("Project not found: ID " + id);
    }
}