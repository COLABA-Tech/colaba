package com.example.colaba.shared.exception.tag;

import com.example.colaba.shared.exception.common.NotFoundException;

public class TagNotFoundException extends NotFoundException {
    public TagNotFoundException(Long id) {
        super("Tag not found: ID " + id);
    }
}
