import com.example.colaba.file.application.dto.FileResponse;
import com.example.colaba.file.application.dto.UploadFileRequest;
import com.example.colaba.file.infrastructure.controller.InternalFileController;
import com.example.colaba.file.infrastructure.service.FileServiceFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class InternalFileControllerTest {

    private FileServiceFacade fileService;
    private InternalFileController controller;
    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        fileService = mock(FileServiceFacade.class);
        controller = new InternalFileController(fileService);

        // Disable security for tests since we're testing the controller logic, not security
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                // Add any necessary filters or advice here if needed
                .build();
    }

    @Test
    void getByTaskIdShouldReturnFileDtos() throws Exception {
        FileResponse response1 = FileResponse.builder()
                .id(1L)
                .originalFilename("file1.txt")
                .build();
        FileResponse response2 = FileResponse.builder()
                .id(2L)
                .originalFilename("file2.txt")
                .build();

        when(fileService.getAttachments(100L)).thenReturn(List.of(response1, response2));

        // Fixed URL to match controller mapping
        mockMvc.perform(get("/api/files/internal/files")
                        .param("taskId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(fileService, times(1)).getAttachments(100L);
    }

    @Test
    void uploadFilesShouldReturnFileDtos() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile("files", "file1.txt",
                "text/plain", "content1".getBytes());

        FileResponse response = FileResponse.builder()
                .id(1L)
                .originalFilename("file1.txt")
                .build();

        when(fileService.uploadFiles(any(UploadFileRequest.class))).thenReturn(List.of(response));

        // Fixed URL to match controller mapping
        mockMvc.perform(multipart("/api/files/internal/files")
                        .file(file1)
                        .param("taskId", "100")
                        .param("uploadedBy", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].originalFilename").value("file1.txt"));

        verify(fileService, times(1)).uploadFiles(any(UploadFileRequest.class));
    }

    @Test
    void downloadFileShouldReturnResource() throws Exception {
        FileResponse metadata = FileResponse.builder()
                .id(1L)
                .originalFilename("file1.txt")
                .contentType("text/plain")
                .size(123L)
                .build();

        Resource resource = new ByteArrayResource("test content".getBytes());

        when(fileService.getFileMetadata(1L)).thenReturn(metadata);
        when(fileService.getFileContent(1L)).thenReturn(resource);

        // Fixed URL to match controller mapping
        mockMvc.perform(get("/api/files/internal/files/1/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"file1.txt\""))
                .andExpect(header().string("Content-Length", "123"))
                .andExpect(content().bytes("test content".getBytes()));

        verify(fileService, times(1)).getFileMetadata(1L);
        verify(fileService, times(1)).getFileContent(1L);
    }
}