package com.example.colaba.shared.common.domain.exception.tag;

import com.example.colaba.shared.common.domain.exception.common.NotFoundException;

public class TagNotFoundException extends NotFoundException {
    public TagNotFoundException(Long id) {
        super("Tag not found: ID " + id);
    }
}
