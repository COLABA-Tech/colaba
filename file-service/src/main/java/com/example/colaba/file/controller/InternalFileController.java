package com.example.colaba.file.controller;

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

    // Загрузка файлов (вызывается из task-service)
    @PostMapping("/files")
    public ResponseEntity<List<FileDto>> upload(
            @RequestParam("taskId") Long taskId,
            @RequestParam("uploadedBy") Long uploadedBy,
            @RequestParam("files") List<MultipartFile> files) {

        List<FileDto> result = fileService.uploadFiles(taskId, uploadedBy, files);
        return ResponseEntity.ok(result);
    }

    // Список attachments для задачи
    @GetMapping("/files")
    public ResponseEntity<List<FileDto>> getByTaskId(@RequestParam("taskId") Long taskId) {
        List<FileDto> attachments = fileService.getAttachments(taskId);
        return ResponseEntity.ok(attachments);
    }

    // Скачивание файла по его id
    @GetMapping("/files/{fileId}/content")
    public ResponseEntity<Resource> download(@PathVariable Long fileId) {
        Resource resource = fileService.getFileContent(fileId);

        // Можно добавить оригинальное имя в заголовок
        // String filename = ... получить из БД, если нужно

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"file\"")
                .body(resource);
    }
}