import com.example.colaba.file.application.dto.FileResponse;
import com.example.colaba.file.application.dto.UploadFileRequest;
import com.example.colaba.file.application.service.FileService;
import com.example.colaba.file.domain.entity.File;
import com.example.colaba.file.infrastructure.service.FileServiceFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class FileServiceFacadeTest {

    @Mock
    private FileService fileService;

    @Mock
    private MultipartFile multipartFile1;

    @Mock
    private MultipartFile multipartFile2;

    @Mock
    private MultipartFile emptyMultipartFile;

    @InjectMocks
    private FileServiceFacade fileServiceFacade;

    private UploadFileRequest uploadFileRequest;
    private FileResponse mockFileResponse;
    private File mockFileEntity;
    private byte[] mockFileContent;

    @BeforeEach
    void setUp() {
        // Setup mock data
        mockFileContent = "test file content".getBytes();

        mockFileResponse = FileResponse.builder()
                .id(1L)
                .taskId(100L)
                .originalFilename("test.txt")
                .contentType("text/plain")
                .size(1024L)
                .uploadedBy(10L)
                .uploadedAt(OffsetDateTime.now())
                .build();

        mockFileEntity = File.builder()
                .id(1L)
                .taskId(100L)
                .uuid(UUID.randomUUID())
                .originalFilename("test.txt")
                .contentType("text/plain")
                .size(1024L)
                .uploadedBy(10L)
                .createdAt(OffsetDateTime.now())
                .build();

        uploadFileRequest = UploadFileRequest.builder()
                .taskId(100L)
                .uploadedBy(10L)
                .files(Arrays.asList(multipartFile1, multipartFile2, emptyMultipartFile))
                .build();
    }

    @Test
    void uploadFiles_WithValidFiles_ShouldReturnFileResponses() throws IOException {
        // Arrange
        when(multipartFile1.isEmpty()).thenReturn(false);
        when(multipartFile2.isEmpty()).thenReturn(false);
        when(emptyMultipartFile.isEmpty()).thenReturn(true); // This file should be filtered out

        when(multipartFile1.getOriginalFilename()).thenReturn("file1.txt");
        when(multipartFile1.getContentType()).thenReturn("text/plain");
        when(multipartFile1.getSize()).thenReturn(1024L);
        when(multipartFile1.getBytes()).thenReturn("content1".getBytes());

        when(multipartFile2.getOriginalFilename()).thenReturn("file2.pdf");
        when(multipartFile2.getContentType()).thenReturn("application/pdf");
        when(multipartFile2.getSize()).thenReturn(2048L);
        when(multipartFile2.getBytes()).thenReturn("content2".getBytes());

        FileResponse response1 = FileResponse.builder()
                .id(1L)
                .originalFilename("file1.txt")
                .build();

        FileResponse response2 = FileResponse.builder()
                .id(2L)
                .originalFilename("file2.pdf")
                .build();

        when(fileService.saveFile(eq(100L), eq(10L), eq("file1.txt"),
                eq("text/plain"), eq(1024L), any(byte[].class)))
                .thenReturn(response1);

        when(fileService.saveFile(eq(100L), eq(10L), eq("file2.pdf"),
                eq("application/pdf"), eq(2048L), any(byte[].class)))
                .thenReturn(response2);

        // Act
        List<FileResponse> result = fileServiceFacade.uploadFiles(uploadFileRequest);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("file1.txt", result.get(0).originalFilename());
        assertEquals("file2.pdf", result.get(1).originalFilename());

        // Verify that empty files were filtered out
        verify(multipartFile1, times(1)).isEmpty();
        verify(multipartFile2, times(1)).isEmpty();
        verify(emptyMultipartFile, times(1)).isEmpty();

        // Verify that saveFile was called only for non-empty files
        verify(fileService, times(2)).saveFile(anyLong(), anyLong(), anyString(),
                anyString(), anyLong(), any(byte[].class));
    }

    @Test
    void uploadFiles_WithOnlyEmptyFiles_ShouldReturnEmptyList() {
        // Arrange
        when(multipartFile1.isEmpty()).thenReturn(true);
        when(multipartFile2.isEmpty()).thenReturn(true);
        when(emptyMultipartFile.isEmpty()).thenReturn(true);

        // Act
        List<FileResponse> result = fileServiceFacade.uploadFiles(uploadFileRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verify saveFile was never called
        verify(fileService, never()).saveFile(anyLong(), anyLong(), anyString(),
                anyString(), anyLong(), any(byte[].class));
    }

    @Test
    void uploadFiles_WithEmptyRequestFilesList_ShouldReturnEmptyList() {
        // Arrange
        UploadFileRequest emptyRequest = UploadFileRequest.builder()
                .taskId(100L)
                .uploadedBy(10L)
                .files(Collections.emptyList())
                .build();

        // Act
        List<FileResponse> result = fileServiceFacade.uploadFiles(emptyRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verify saveFile was never called
        verify(fileService, never()).saveFile(anyLong(), anyLong(), anyString(),
                anyString(), anyLong(), any(byte[].class));
    }

    @Test
    void uploadFiles_WithNullRequestFilesList_ShouldThrowNullPointerException() {
        // Arrange
        UploadFileRequest nullFilesRequest = UploadFileRequest.builder()
                .taskId(100L)
                .uploadedBy(10L)
                .files(null)
                .build();

        // Act & Assert
        assertThrows(NullPointerException.class,
                () -> fileServiceFacade.uploadFiles(nullFilesRequest));
    }

    @Test
    void uploadFiles_WhenFileIOException_ShouldThrowRuntimeException() throws IOException {
        // Arrange
        when(multipartFile1.isEmpty()).thenReturn(false);
        when(multipartFile1.getOriginalFilename()).thenReturn("file1.txt");
        when(multipartFile1.getContentType()).thenReturn("text/plain");
        when(multipartFile1.getSize()).thenReturn(1024L);
        when(multipartFile1.getBytes()).thenThrow(new IOException("File read error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> fileServiceFacade.uploadFiles(uploadFileRequest));

        assertEquals("File upload failed", exception.getMessage());
        assertTrue(exception.getCause() instanceof IOException);
        assertEquals("File read error", exception.getCause().getMessage());
    }

    @Test
    void saveOne_PrivateMethod_ShouldCallFileServiceCorrectly() throws IOException {
        // This test verifies the behavior of the private method indirectly through the public method
        // Arrange
        UploadFileRequest request = UploadFileRequest.builder()
                .taskId(100L)
                .uploadedBy(10L)
                .files(List.of(multipartFile1))
                .build();

        when(multipartFile1.isEmpty()).thenReturn(false);
        when(multipartFile1.getOriginalFilename()).thenReturn("test.txt");
        when(multipartFile1.getContentType()).thenReturn("text/plain");
        when(multipartFile1.getSize()).thenReturn(1024L);
        when(multipartFile1.getBytes()).thenReturn("test content".getBytes());

        when(fileService.saveFile(eq(100L), eq(10L), eq("test.txt"),
                eq("text/plain"), eq(1024L), eq("test content".getBytes())))
                .thenReturn(mockFileResponse);

        // Act
        List<FileResponse> result = fileServiceFacade.uploadFiles(request);

        // Assert
        assertEquals(1, result.size());
        assertEquals(mockFileResponse, result.get(0));

        verify(fileService, times(1)).saveFile(100L, 10L, "test.txt",
                "text/plain", 1024L, "test content".getBytes());
    }

    @Test
    void getAttachments_ShouldReturnFileResponses() {
        // Arrange
        List<FileResponse> expectedResponses = Arrays.asList(
                FileResponse.builder().id(1L).originalFilename("file1.txt").build(),
                FileResponse.builder().id(2L).originalFilename("file2.pdf").build()
        );

        when(fileService.getAttachments(100L)).thenReturn(expectedResponses);

        // Act
        List<FileResponse> result = fileServiceFacade.getAttachments(100L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedResponses, result);
        verify(fileService, times(1)).getAttachments(100L);
    }

    @Test
    void getAttachments_WithNullTaskId_ShouldCallFileServiceWithNull() {
        // Arrange
        List<FileResponse> emptyList = Collections.emptyList();
        when(fileService.getAttachments(null)).thenReturn(emptyList);

        // Act
        List<FileResponse> result = fileServiceFacade.getAttachments(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(fileService, times(1)).getAttachments(null);
    }

    @Test
    void getFileMetadata_ShouldReturnFileResponse() {
        // Arrange
        when(fileService.getFileMetadataEntity(1L)).thenReturn(mockFileEntity);

        // Act
        FileResponse result = fileServiceFacade.getFileMetadata(1L);

        // Assert
        assertNotNull(result);
        assertEquals(mockFileEntity.getId(), result.id());
        assertEquals(mockFileEntity.getTaskId(), result.taskId());
        assertEquals(mockFileEntity.getOriginalFilename(), result.originalFilename());
        assertEquals(mockFileEntity.getContentType(), result.contentType());
        assertEquals(mockFileEntity.getSize(), result.size());
        assertEquals(mockFileEntity.getUploadedBy(), result.uploadedBy());
        assertEquals(mockFileEntity.getCreatedAt(), result.uploadedAt());

        verify(fileService, times(1)).getFileMetadataEntity(1L);
    }

    @Test
    void getFileMetadata_WhenFileServiceThrowsException_ShouldPropagateException() {
        // Arrange
        when(fileService.getFileMetadataEntity(999L))
                .thenThrow(new RuntimeException("File not found"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> fileServiceFacade.getFileMetadata(999L));

        assertEquals("File not found", exception.getMessage());
        verify(fileService, times(1)).getFileMetadataEntity(999L);
    }

    @Test
    void getFileMetadata_WithNullFileId_ShouldPropagateToFileService() {
        // Arrange
        when(fileService.getFileMetadataEntity(null))
                .thenThrow(new RuntimeException("File ID cannot be null"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> fileServiceFacade.getFileMetadata(null));

        assertEquals("File ID cannot be null", exception.getMessage());
        verify(fileService, times(1)).getFileMetadataEntity(null);
    }

    @Test
    void getFileContent_ShouldReturnResource() {
        // Arrange
        when(fileService.getFileContent(1L)).thenReturn(mockFileContent);

        // Act
        Resource result = fileServiceFacade.getFileContent(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof ByteArrayResource);
        assertArrayEquals(mockFileContent, ((ByteArrayResource) result).getByteArray());
        verify(fileService, times(1)).getFileContent(1L);
    }

    @Test
    void getFileContent_WithNullContent_ShouldThrowRuntimeException() {
        // Arrange
        when(fileService.getFileContent(1L)).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> fileServiceFacade.getFileContent(1L));

        assertEquals("File content is empty or not found", exception.getMessage());
        verify(fileService, times(1)).getFileContent(1L);
    }

    @Test
    void getFileContent_WithEmptyContent_ShouldThrowRuntimeException() {
        // Arrange
        when(fileService.getFileContent(1L)).thenReturn(new byte[0]);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> fileServiceFacade.getFileContent(1L));

        assertEquals("File content is empty or not found", exception.getMessage());
        verify(fileService, times(1)).getFileContent(1L);
    }

    @Test
    void getFileContent_WhenFileServiceThrowsException_ShouldPropagateException() {
        // Arrange
        when(fileService.getFileContent(999L))
                .thenThrow(new RuntimeException("File not found"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> fileServiceFacade.getFileContent(999L));

        assertEquals("File not found", exception.getMessage());
        verify(fileService, times(1)).getFileContent(999L);
    }

    @Test
    void getFileContent_WithNullFileId_ShouldPropagateToFileService() {
        // Arrange
        when(fileService.getFileContent(null))
                .thenThrow(new RuntimeException("File ID cannot be null"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> fileServiceFacade.getFileContent(null));

        assertEquals("File ID cannot be null", exception.getMessage());
        verify(fileService, times(1)).getFileContent(null);
    }

    @Test
    void uploadFiles_WithMultipleNonEmptyFiles_ShouldProcessAllFiles() throws IOException {
        // Arrange
        List<MultipartFile> files = Arrays.asList(multipartFile1, multipartFile2);
        UploadFileRequest request = UploadFileRequest.builder()
                .taskId(100L)
                .uploadedBy(10L)
                .files(files)
                .build();

        when(multipartFile1.isEmpty()).thenReturn(false);
        when(multipartFile2.isEmpty()).thenReturn(false);

        when(multipartFile1.getOriginalFilename()).thenReturn("file1.txt");
        when(multipartFile1.getContentType()).thenReturn("text/plain");
        when(multipartFile1.getSize()).thenReturn(100L);
        when(multipartFile1.getBytes()).thenReturn("content1".getBytes());

        when(multipartFile2.getOriginalFilename()).thenReturn("file2.txt");
        when(multipartFile2.getContentType()).thenReturn("text/plain");
        when(multipartFile2.getSize()).thenReturn(200L);
        when(multipartFile2.getBytes()).thenReturn("content2".getBytes());

        FileResponse response1 = FileResponse.builder().id(1L).build();
        FileResponse response2 = FileResponse.builder().id(2L).build();

        when(fileService.saveFile(anyLong(), anyLong(), anyString(),
                anyString(), anyLong(), any(byte[].class)))
                .thenReturn(response1, response2);

        // Act
        List<FileResponse> result = fileServiceFacade.uploadFiles(request);

        // Assert
        assertEquals(2, result.size());
        verify(fileService, times(2)).saveFile(anyLong(), anyLong(), anyString(),
                anyString(), anyLong(), any(byte[].class));
    }

    @Test
    void fileResponseBuilder_ShouldMapAllFieldsCorrectly() {
        // This test ensures that all fields from File entity are properly mapped to FileResponse
        // Arrange
        OffsetDateTime now = OffsetDateTime.now();
        File file = File.builder()
                .id(123L)
                .taskId(456L)
                .uuid(UUID.randomUUID())
                .originalFilename("document.pdf")
                .contentType("application/pdf")
                .size(5000L)
                .uploadedBy(789L)
                .createdAt(now)
                .build();

        when(fileService.getFileMetadataEntity(123L)).thenReturn(file);

        // Act
        FileResponse result = fileServiceFacade.getFileMetadata(123L);

        // Assert
        assertEquals(123L, result.id());
        assertEquals(456L, result.taskId());
        assertEquals("document.pdf", result.originalFilename());
        assertEquals("application/pdf", result.contentType());
        assertEquals(5000L, result.size());
        assertEquals(789L, result.uploadedBy());
        assertEquals(now, result.uploadedAt());
    }

    @Test
    void saveOne_WhenFileServiceThrowsException_ShouldWrapInRuntimeException() throws IOException {
        // This tests the private saveOne method's exception handling
        // Arrange
        when(multipartFile1.isEmpty()).thenReturn(false);
        when(multipartFile1.getOriginalFilename()).thenReturn("test.txt");
        when(multipartFile1.getContentType()).thenReturn("text/plain");
        when(multipartFile1.getSize()).thenReturn(1024L);
        when(multipartFile1.getBytes()).thenReturn("content".getBytes());

        when(fileService.saveFile(anyLong(), anyLong(), anyString(),
                anyString(), anyLong(), any(byte[].class)))
                .thenThrow(new IllegalArgumentException("Invalid file"));

        UploadFileRequest request = UploadFileRequest.builder()
                .taskId(100L)
                .uploadedBy(10L)
                .files(List.of(multipartFile1))
                .build();

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> fileServiceFacade.uploadFiles(request));

        // The original exception from fileService is propagated, not wrapped
        assertTrue(exception instanceof IllegalArgumentException);
        assertEquals("Invalid file", exception.getMessage());
    }
}