package com.example.colaba.shared.webmvc.infrastructure.client;

import com.example.colaba.shared.common.application.dto.file.FileDto;
import com.example.colaba.shared.webmvc.infrastructure.feign.FileServiceFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@FeignClient(
        name = "file-service",
        path = "/api/files/internal",
        configuration = {FileServiceFeignConfig.class}
)
public interface FileServiceClient {
    @PostMapping(
            value = "/files",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    List<FileDto> uploadFiles(
            @RequestParam("taskId") Long taskId,
            @RequestParam("uploadedBy") Long uploadedBy,
            @RequestPart("files") List<MultipartFile> files);

    @GetMapping("/files")
    List<FileDto> getFilesByTaskId(@RequestParam("taskId") Long taskId);

    @GetMapping("/files/{fileId}/download")
    ResponseEntity<Resource> downloadFile(@PathVariable Long fileId);

    @GetMapping("/files/{fileId}/metadata")
    FileDto getFileMetadata(@PathVariable Long fileId);
}