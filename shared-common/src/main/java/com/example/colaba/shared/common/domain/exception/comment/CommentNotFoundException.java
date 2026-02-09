package com.example.colaba.shared.common.domain.exception.comment;

import com.example.colaba.shared.common.domain.exception.common.NotFoundException;

public class CommentNotFoundException extends NotFoundException {
    public CommentNotFoundException(Long id) {
        super("Comment not found: " + id);
    }
}