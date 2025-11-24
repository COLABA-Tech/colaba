package com.example.colaba.shared.exception.comment;

import com.example.colaba.shared.exception.common.NotFoundException;

public class CommentNotFoundException extends NotFoundException {
    public CommentNotFoundException(Long id) {
        super("Comment not found: " + id);
    }
}