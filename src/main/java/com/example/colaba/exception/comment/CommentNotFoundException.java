package com.example.colaba.exception.comment;

public class CommentNotFoundException extends RuntimeException {
    public CommentNotFoundException(String message) {
        super(message);
    }

    public CommentNotFoundException(Long id) {
        super("Comment not found: " + id);
    }
}