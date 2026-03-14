package com.example.colaba.file.infrastructure.config;

import com.example.colaba.file.application.mapper.FileMapper;
import com.example.colaba.file.application.ports.FileRepositoryPort;
import com.example.colaba.file.application.ports.FileStoragePort;
import com.example.colaba.file.application.service.FileService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FileServiceConfig {

    @Bean
    public FileService fileService(
            FileRepositoryPort fileRepository,
            FileStoragePort fileStorage
    ) {
        return new FileService(

                fileRepository,
                fileStorage,
                new FileMapper()
        );
    }
}
