package com.example.colaba.exception.project;

import com.example.colaba.exception.common.DuplicateEntityException;

public class DuplicateProjectNameException extends DuplicateEntityException {

    public DuplicateProjectNameException(String name) {
        super("Project with name '" + name + "' already exists");
    }
}