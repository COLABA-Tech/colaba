package com.example.colaba.shared.common.exception.project;

import com.example.colaba.shared.common.exception.common.DuplicateEntityException;

public class DuplicateProjectNameException extends DuplicateEntityException {

    public DuplicateProjectNameException(String name) {
        super("Project with name '" + name + "' already exists");
    }
}