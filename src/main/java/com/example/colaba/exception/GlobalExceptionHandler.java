package com.example.colaba.exception;

import com.example.colaba.exception.common.DuplicateEntityException;
import com.example.colaba.exception.common.NotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handleNotFound(NotFoundException e) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(DuplicateEntityException.class)
    public ResponseEntity<String> handleDuplicate(DuplicateEntityException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
