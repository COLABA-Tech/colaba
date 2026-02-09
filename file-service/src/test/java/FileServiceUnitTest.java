import com.example.colaba.file.application.mapper.FileMapper;
import com.example.colaba.file.application.ports.FileRepositoryPort;
import com.example.colaba.file.application.ports.FileStoragePort;
import com.example.colaba.file.application.service.FileService;
import com.example.colaba.file.domain.entity.File;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class FileServiceUnitTest {

    @Mock
    FileRepositoryPort fileRepository;

    @Mock
    FileStoragePort fileStorage;

    FileService fileService;

    @BeforeEach
    void setUp() {
        fileService = new FileService(
                fileRepository,
                fileStorage,
                new FileMapper()
        );
    }

    @Test
    void saveFile_shouldPersistFileAndStoreContent() {
        byte[] content = "hello".getBytes();

        File saved = File.builder()
                .id(1L)
                .originalFilename("test.txt")
                .build();

        when(fileRepository.save(any())).thenReturn(saved);

        var response = fileService.saveFile(
                1L, 2L, "test.txt", "text/plain", content.length, content
        );

        assertEquals("test.txt", response.originalFilename());
        verify(fileStorage).save(any(UUID.class), eq(content));
        verify(fileRepository).save(any());
    }

    @Test
    void saveFile_shouldFailForEmptyFile() {
        assertThrows(IllegalArgumentException.class, () ->
                fileService.saveFile(1L, 1L, "a.txt", "text", 0, new byte[0])
        );
    }

    @Test
    void getFiles_shouldReturnAllFileNames() {
        when(fileRepository.findAll()).thenReturn(List.of(
                File.builder().originalFilename("a.txt").build(),
                File.builder().originalFilename("b.txt").build()
        ));

        List<String> files = fileService.getFiles();

        assertEquals(2, files.size());
        assertTrue(files.contains("a.txt"));
        assertTrue(files.contains("b.txt"));
    }

    @Test
    void getAttachments_shouldMapEntitiesToDtos() {
        when(fileRepository.findAllByTaskId(10L)).thenReturn(List.of(
                File.builder().id(1L).originalFilename("a.txt").build()
        ));

        var result = fileService.getAttachments(10L);

        assertEquals(1, result.size());
        assertEquals("a.txt", result.get(0).originalFilename());
    }

    @Test
    void getFileMetadataEntity_shouldReturnFile() {
        File file = File.builder().id(5L).build();
        when(fileRepository.findById(5L)).thenReturn(Optional.of(file));

        File result = fileService.getFileMetadataEntity(5L);

        assertSame(file, result);
    }

    @Test
    void getFileMetadataEntity_shouldThrowIfNotFound() {
        when(fileRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                fileService.getFileMetadataEntity(99L)
        );
    }

    @Test
    void getFileContent_shouldReturnFileContent() {
        // Arrange
        UUID fileUuid = UUID.randomUUID();
        File mockFile = File.builder()
                .id(1L)
                .uuid(fileUuid)
                .originalFilename("test.txt")
                .build();

        byte[] expectedContent = "Hello, World!".getBytes();

        when(fileRepository.findById(1L)).thenReturn(Optional.of(mockFile));
        when(fileStorage.getFileContent(fileUuid)).thenReturn(expectedContent);

        // Act
        byte[] actualContent = fileService.getFileContent(1L);

        // Assert
        assertNotNull(actualContent);
        assertArrayEquals(expectedContent, actualContent);
        verify(fileRepository).findById(1L);
        verify(fileStorage).getFileContent(fileUuid);
    }

    @Test
    void getFileContent_shouldThrowWhenFileNotFound() {
        // Arrange
        when(fileRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                fileService.getFileContent(99L)
        );

        assertEquals("File not found", exception.getMessage());
        verify(fileRepository).findById(99L);
        verify(fileStorage, never()).getFileContent(any());
    }

    @Test
    void getFileContent_shouldPropagateStorageException() {
        // Arrange
        UUID fileUuid = UUID.randomUUID();
        File mockFile = File.builder()
                .id(4L)
                .uuid(fileUuid)
                .originalFilename("test.txt")
                .build();

        when(fileRepository.findById(4L)).thenReturn(Optional.of(mockFile));
        when(fileStorage.getFileContent(fileUuid))
                .thenThrow(new RuntimeException("Storage error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                fileService.getFileContent(4L)
        );

        assertEquals("Storage error", exception.getMessage());
        verify(fileRepository).findById(4L);
        verify(fileStorage).getFileContent(fileUuid);
    }

    @Test
    void getFileContent_shouldCallGetFileMetadataEntity() {
        // Arrange
        UUID fileUuid = UUID.randomUUID();
        File mockFile = File.builder()
                .id(5L)
                .uuid(fileUuid)
                .originalFilename("test.txt")
                .build();

        byte[] expectedContent = "Test content".getBytes();

        // Mock the getFileMetadataEntity method indirectly through findById
        when(fileRepository.findById(5L)).thenReturn(Optional.of(mockFile));
        when(fileStorage.getFileContent(fileUuid)).thenReturn(expectedContent);

        // Create a spy to verify the internal method call
        FileService fileServiceSpy = spy(fileService);
        doReturn(mockFile).when(fileServiceSpy).getFileMetadataEntity(5L);

        // Act
        byte[] result = fileServiceSpy.getFileContent(5L);

        // Assert
        assertArrayEquals(expectedContent, result);
        verify(fileServiceSpy).getFileMetadataEntity(5L);
        verify(fileStorage).getFileContent(fileUuid);
    }

    @Test
    void saveFile_shouldFailForNullContent() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                fileService.saveFile(1L, 1L, "a.txt", "text/plain", 0, null)
        );

        assertEquals("File is empty", exception.getMessage());
        verify(fileStorage, never()).save(any(), any());
        verify(fileRepository, never()).save(any());
    }
}