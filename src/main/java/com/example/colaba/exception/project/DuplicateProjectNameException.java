package com.example.colaba.exception.project;

import com.example.colaba.exception.common.DuplicateEntityException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class DuplicateProjectNameException extends DuplicateEntityException {

    public DuplicateProjectNameException(String name) {
        super("Project with name '" + name + "' already exists");
    }
}