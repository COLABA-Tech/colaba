package com.example.colaba.file;

import com.example.colaba.file.dto.FileDto;
import com.example.colaba.file.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalFileController {

    private final FileService fileService;

    @PostMapping("/files")
    public ResponseEntity<List<FileDto>> uploadFiles(
            @RequestParam Long taskId,
            @RequestParam Long uploadedBy,
            @RequestParam("files") List<MultipartFile> files) {

        List<FileDto> uploaded = fileService.uploadFiles(taskId, uploadedBy, files);
        return ResponseEntity.ok(uploaded);
    }

    @GetMapping("/files")
    public ResponseEntity<List<FileDto>> getAttachments(@RequestParam Long taskId) {
        List<FileDto> attachments = fileService.getAttachments(taskId);
        return ResponseEntity.ok(attachments);
    }
    @GetMapping("/files/{attachmentId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long attachmentId) {
        FileDto metadata = fileService.getFileMetadata(attachmentId);

        Resource resource = fileService.getFileContent(attachmentId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(metadata.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.getOriginalFilename() + "\"")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(metadata.getSize()))
                .body(resource);
    }
}