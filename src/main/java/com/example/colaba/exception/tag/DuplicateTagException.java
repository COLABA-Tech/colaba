package com.example.colaba.exception.tag;

import com.example.colaba.exception.common.DuplicateEntityException;

public class DuplicateTagException extends DuplicateEntityException {
    public DuplicateTagException(String name, Long projectId) {
        super("Tag '" + name + "' already exists in project " + projectId);
    }
}
