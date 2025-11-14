package com.example.colaba.exception.comment;

import com.example.colaba.exception.common.NotFoundException;

public class CommentNotFoundException extends NotFoundException {
    public CommentNotFoundException(Long id) {
        super("Comment not found: " + id);
    }
}