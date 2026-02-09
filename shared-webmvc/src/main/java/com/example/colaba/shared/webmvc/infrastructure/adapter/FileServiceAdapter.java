package com.example.colaba.shared.webmvc.infrastructure.adapter;

import com.example.colaba.shared.common.application.dto.file.FileDto;
import com.example.colaba.shared.webmvc.application.ports.FileServicePort;
import com.example.colaba.shared.webmvc.infrastructure.circuit.FileServiceClientWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FileServiceAdapter implements FileServicePort {
    private final FileServiceClientWrapper wrapper;

    @Override
    public List<FileDto> uploadFiles(Long taskId, Long uploadedBy, List<MultipartFile> files) {
        return wrapper.uploadFiles(taskId, uploadedBy, files);
    }

    @Override
    public List<FileDto> getFilesByTaskId(Long taskId) {
        return wrapper.getFilesByTaskId(taskId);
    }

    @Override
    public ResponseEntity<Resource> downloadFile(Long fileId) {
        return wrapper.downloadFile(fileId);
    }

    @Override
    public FileDto getFileMetadata(Long fileId) {
        return wrapper.getFileMetadata(fileId);
    }
}