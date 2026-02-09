package com.example.colaba.file.application.ports;

import java.util.UUID;

public interface FileStoragePort {
    void save(UUID uuid, byte[] content);

    byte[] getFileContent(UUID uuid);
}
