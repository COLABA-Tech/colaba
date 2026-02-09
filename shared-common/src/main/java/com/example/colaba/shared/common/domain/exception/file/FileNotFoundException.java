package com.example.colaba.shared.common.domain.exception.file;

import com.example.colaba.shared.common.domain.exception.common.NotFoundException;

public class FileNotFoundException extends NotFoundException {
    public FileNotFoundException(Long id) {
        super("File not found: ID " + id);
    }
}