import com.example.colaba.file.application.dto.FileResponse;
import com.example.colaba.file.application.dto.UploadFileRequest;
import com.example.colaba.file.infrastructure.controller.InternalFileController;
import com.example.colaba.file.infrastructure.service.FileServiceFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InternalFileControllerUnitTest {

    private InternalFileController controller;
    private FileServiceFacade fileService;

    @BeforeEach
    void setup() {
        fileService = mock(FileServiceFacade.class);
        controller = new InternalFileController(fileService);
    }

    @Test
    void getByTaskIdShouldReturnAttachments() {
        FileResponse response1 = FileResponse.builder()
                .id(1L)
                .originalFilename("file1.txt")
                .build();
        FileResponse response2 = FileResponse.builder()
                .id(2L)
                .originalFilename("file2.txt")
                .build();

        when(fileService.getAttachments(100L)).thenReturn(List.of(response1, response2));

        ResponseEntity<List<FileResponse>> response = controller.getByTaskId(100L);

        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("file1.txt", response.getBody().get(0).originalFilename());
        assertEquals("file2.txt", response.getBody().get(1).originalFilename());
        verify(fileService, times(1)).getAttachments(100L);
    }

    @Test
    void uploadShouldReturnUploadedFiles() {
        FileResponse response1 = FileResponse.builder()
                .id(1L)
                .originalFilename("file1.txt")
                .build();
        FileResponse response2 = FileResponse.builder()
                .id(2L)
                .originalFilename("file2.txt")
                .build();

        // Note: Adjust mock based on actual FileService method signature
        when(fileService.uploadFiles(any(UploadFileRequest.class))).thenReturn(List.of(response1, response2));

        ResponseEntity<List<FileResponse>> response = controller.upload(
                100L,
                10L,
                List.of(mock(MultipartFile.class), mock(MultipartFile.class))
        );

        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(fileService, times(1)).uploadFiles(any(UploadFileRequest.class));
    }

    @Test
    void downloadFileShouldReturnResource() {
        FileResponse metadata = FileResponse.builder()
                .id(1L)
                .originalFilename("file1.txt")
                .contentType("text/plain")
                .size(123L)
                .build();

        Resource resource = new ByteArrayResource("test content".getBytes());

        when(fileService.getFileMetadata(1L)).thenReturn(metadata);
        when(fileService.getFileContent(1L)).thenReturn(resource);

        ResponseEntity<Resource> response = controller.downloadFile(1L);

        assertNotNull(response.getBody());
        assertEquals(resource, response.getBody());
        assertEquals("text/plain", response.getHeaders().getContentType().toString());
        assertTrue(response.getHeaders().getContentDisposition().toString().contains("file1.txt"));
        assertEquals(123L, response.getHeaders().getContentLength());

        verify(fileService, times(1)).getFileMetadata(1L);
        verify(fileService, times(1)).getFileContent(1L);
    }
}