package com.example.colaba.shared.webmvc.infrastructure.circuit;

import com.example.colaba.shared.common.application.dto.file.FileDto;
import com.example.colaba.shared.webmvc.infrastructure.client.FileServiceClient;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FileServiceClientWrapper {

    private final FileServiceClient client;
    private final CircuitBreakerRegistry registry;

    private CircuitBreaker cb() {
        return registry.circuitBreaker("file-service");
    }

    public List<FileDto> uploadFiles(Long taskId, Long uploadedBy, List<MultipartFile> files) {
        return cb().executeSupplier(() -> client.uploadFiles(taskId, uploadedBy, files));
    }

    public List<FileDto> getFilesByTaskId(Long taskId) {
        return cb().executeSupplier(() -> client.getFilesByTaskId(taskId));
    }

    public ResponseEntity<Resource> downloadFile(Long fileId) {
        return cb().executeSupplier(() -> client.downloadFile(fileId));
    }

    public FileDto getFileMetadata(Long fileId) {
        return cb().executeSupplier(() -> client.getFileMetadata(fileId));
    }
}