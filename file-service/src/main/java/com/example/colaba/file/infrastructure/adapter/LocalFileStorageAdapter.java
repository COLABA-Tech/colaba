package com.example.colaba.file.infrastructure.adapter;

import com.example.colaba.file.application.ports.FileStoragePort;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
public class LocalFileStorageAdapter implements FileStoragePort {

    @Value("${app.upload-dir:/uploads}")
    private String uploadDirPath;

    private Path uploadDir;

    @PostConstruct
    public void init() throws IOException {
        uploadDir = Path.of(uploadDirPath);
        Files.createDirectories(uploadDir);
    }

    @Override
    public void save(UUID uuid, byte[] content) {
        Path filePath = uploadDir.resolve(uuid.toString());
        try {
            Files.write(filePath, content);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file: " + uuid, e);
        }
    }

    @Override
    public byte[] getFileContent(UUID uuid) {
        Path filePath = uploadDir.resolve(uuid.toString());
        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load file: " + uuid, e);
        }
    }
}