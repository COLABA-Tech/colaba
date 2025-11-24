package com.example.colaba.shared.exception.project;

import com.example.colaba.shared.exception.common.DuplicateEntityException;

public class DuplicateProjectNameException extends DuplicateEntityException {

    public DuplicateProjectNameException(String name) {
        super("Project with name '" + name + "' already exists");
    }
}