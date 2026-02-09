package com.example.colaba.file.controller;

import com.example.colaba.file.service.FileService;
import com.example.colaba.shared.common.dto.file.FileDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/files/internal")
@RequiredArgsConstructor
@Validated
@Tag(name = "Files Internal", description = "Internal Files API for internal file operations")
public class InternalFileController {

    private final FileService fileService;

    @PostMapping(
            value = "/files",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(
            summary = "Upload multiple files",
            description = "Upload any type of files (png, pdf, docx, etc.)"
    )
    public ResponseEntity<List<FileDto>> upload(
            @RequestParam("taskId") @NotNull Long taskId,
            @RequestParam("uploadedBy") @NotNull Long uploadedBy,

            @io.swagger.v3.oas.annotations.Parameter(
                    description = "Files to upload",
                    required = true
            )
            @RequestPart("files") @NotNull List<MultipartFile> files
    ) {
        if (files.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(
                fileService.uploadFiles(taskId, uploadedBy, files)
        );
    }

    @GetMapping("/files")
    @Operation(summary = "Get files by task ID",
            description = "Retrieve metadata of all files attached to a specific task")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Files retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid task ID"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "200", description = "No files found (returns empty list)")
    })
    public ResponseEntity<List<FileDto>> getByTaskId(
            @RequestParam("taskId") @NotNull Long taskId) {

        List<FileDto> attachments = fileService.getAttachments(taskId);
        return ResponseEntity.ok(attachments);
    }

    @GetMapping("/files/{fileId}/download")
    @Operation(summary = "Download file",
            description = "Download a specific file by its ID. Returns the file content with appropriate headers.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "File downloaded successfully"),
            @ApiResponse(responseCode = "404", description = "File not found"),
            @ApiResponse(responseCode = "410", description = "File content is no longer available"),
            @ApiResponse(responseCode = "500", description = "Error reading file from storage")
    })
    public ResponseEntity<Resource> downloadFile(@PathVariable @NotNull Long fileId) {
        FileDto metadata = fileService.getFileMetadata(fileId);
        Resource resource = fileService.getFileContent(fileId);

        if (resource == null || !resource.exists()) {
            return ResponseEntity.status(HttpStatus.GONE).build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(metadata.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + metadata.getOriginalFilename() + "\"")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(metadata.getSize()))
                .body(resource);
    }
}