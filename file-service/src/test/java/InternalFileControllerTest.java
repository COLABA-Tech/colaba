/*
import com.example.colaba.file.controller.InternalFileController;
import com.example.colaba.file.service.FileService;
import com.example.colaba.shared.common.dto.file.FileDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class InternalFileControllerTest {

    private FileService fileService;
    private InternalFileController controller;
    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        fileService = mock(FileService.class);
        controller = new InternalFileController(fileService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }


    @Test
    void helloShouldReturnHelloString() throws Exception {
        mockMvc.perform(get("/internal/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello"));
    }

    @Test
    void getByTaskIdShouldReturnFileDtos() throws Exception {
        FileDto dto1 = FileDto.builder().id(1L).originalFilename("file1.txt").build();
        FileDto dto2 = FileDto.builder().id(2L).originalFilename("file2.txt").build();

        when(fileService.getAttachments(100L)).thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/internal/files")
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

        FileDto dto = FileDto.builder().id(1L).originalFilename("file1.txt").build();
        when(fileService.uploadFiles(100L, 10L, List.of(file1))).thenReturn(List.of(dto));

        mockMvc.perform(multipart("/internal/files")
                        .file(file1)
                        .param("taskId", "100")
                        .param("uploadedBy", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].originalFilename").value("file1.txt"));

        verify(fileService, times(1)).uploadFiles(anyLong(), anyLong(), anyList());
    }

    @Test
    void downloadFileShouldReturnResource() throws Exception {
        UUID uuid = UUID.randomUUID();
        FileDto dto = FileDto.builder()
                .id(1L)
                .originalFilename("file1.txt")
                .contentType("text/plain")
                .size(123L)
                .build();

        Resource resource = new ByteArrayResource("test content".getBytes());

        when(fileService.getFileMetadata(1L)).thenReturn(dto);
        when(fileService.getFileContent(1L)).thenReturn(resource);

        mockMvc.perform(get("/internal/files/1/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"file1.txt\""))
                .andExpect(header().string("Content-Length", "123"))
                .andExpect(content().bytes("test content".getBytes()));

        verify(fileService, times(1)).getFileMetadata(1L);
        verify(fileService, times(1)).getFileContent(1L);
    }
}
 */
