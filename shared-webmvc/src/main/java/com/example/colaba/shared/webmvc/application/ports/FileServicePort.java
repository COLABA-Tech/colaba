package com.example.colaba.shared.webmvc.application.ports;

import com.example.colaba.shared.common.application.dto.file.FileDto;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileServicePort {
    List<FileDto> uploadFiles(Long taskId, Long uploadedBy, List<MultipartFile> files);

    List<FileDto> getFilesByTaskId(Long taskId);

    ResponseEntity<Resource> downloadFile(Long fileId);

    FileDto getFileMetadata(Long fileId);
}