import com.example.colaba.file.controller.InternalFileController;
import com.example.colaba.file.service.FileService;
import com.example.colaba.shared.common.dto.file.FileDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class InternalFileControllerUnitTest {

    private InternalFileController controller;
    private FileService fileService;

    @BeforeEach
    void setup() {
        fileService = mock(FileService.class);
        controller = new InternalFileController(fileService);
    }

    @Test
    void getByTaskIdShouldReturnAttachments() {
        FileDto f1 = FileDto.builder().id(1L).originalFilename("file1.txt").build();
        FileDto f2 = FileDto.builder().id(2L).originalFilename("file2.txt").build();

        when(fileService.getAttachments(100L)).thenReturn(List.of(f1, f2));

        ResponseEntity<List<FileDto>> response = controller.getByTaskId(100L);

        assertThat(response.getBody()).containsExactly(f1, f2);
        verify(fileService, times(1)).getAttachments(100L);
    }
    @Test
    void uploadShouldReturnUploadedFiles() {
        FileDto f1 = FileDto.builder().id(1L).originalFilename("file1.txt").build();
        FileDto f2 = FileDto.builder().id(2L).originalFilename("file2.txt").build();

        when(fileService.uploadFiles(eq(100L), eq(10L), anyList())).thenReturn(List.of(f1, f2));

        ResponseEntity<List<FileDto>> response = controller.upload(100L, 10L, List.of(mock(MultipartFile.class), mock(MultipartFile.class)));

        assertThat(response.getBody()).containsExactly(f1, f2);
        verify(fileService, times(1)).uploadFiles(eq(100L), eq(10L), anyList());
    }
    @Test
    void downloadFileShouldReturnResource() {
        FileDto metadata = FileDto.builder()
                .id(1L)
                .originalFilename("file1.txt")
                .contentType("text/plain")
                .size(123L)
                .build();

        Resource resource = new ByteArrayResource("test content".getBytes());

        when(fileService.getFileMetadata(1L)).thenReturn(metadata);
        when(fileService.getFileContent(1L)).thenReturn(resource);

        ResponseEntity<Resource> response = controller.downloadFile(1L);

        assertThat(response.getBody()).isEqualTo(resource);
        assertThat(response.getHeaders().getContentType().toString()).isEqualTo("text/plain");
        assertThat(response.getHeaders().getContentDisposition().getFilename()).isEqualTo("file1.txt");
        assertThat(response.getHeaders().getContentLength()).isEqualTo(123L);

        verify(fileService, times(1)).getFileMetadata(1L);
        verify(fileService, times(1)).getFileContent(1L);
    }

}
