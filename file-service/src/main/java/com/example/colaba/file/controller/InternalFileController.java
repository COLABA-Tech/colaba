package com.example.colaba.file.controller;

import com.example.colaba.file.service.FileService;
import com.example.colaba.shared.common.dto.file.FileDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/files/internal")
@RequiredArgsConstructor
@Tag(name = "Files Internal", description = "Internal Files API")
public class InternalFileController {

    private final FileService fileService;

    @PostMapping("/files")
    public ResponseEntity<List<FileDto>> upload(
            @RequestParam("taskId") Long taskId,
            @RequestParam("uploadedBy") Long uploadedBy,
            @RequestParam("files") List<MultipartFile> files) {
        List<FileDto> result = fileService.uploadFiles(taskId, uploadedBy, files);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/files")
    public ResponseEntity<List<FileDto>> getByTaskId(@RequestParam("taskId") Long taskId) {
        List<FileDto> attachments = fileService.getAttachments(taskId);
        return ResponseEntity.ok(attachments);
    }

    @GetMapping("/files/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
        FileDto metadata = fileService.getFileMetadata(fileId);
        Resource resource = fileService.getFileContent(fileId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(metadata.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.getOriginalFilename() + "\"")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(metadata.getSize()))
                .body(resource);
    }
}