package com.example.colaba.exception.tag;

import com.example.colaba.exception.common.NotFoundException;

public class TagNotFoundException extends NotFoundException {
    public TagNotFoundException(Long id) {
        super("Tag not found: ID " + id);
    }
}
