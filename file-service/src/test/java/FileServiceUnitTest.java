import com.example.colaba.file.entity.FileJpa;
import com.example.colaba.file.repository.FileRepository;
import com.example.colaba.file.service.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class FileServiceUnitTest {

    private FileService fileService;
    private FileRepository fileRepository;

    @BeforeEach
    void setup() {
        fileRepository = mock(FileRepository.class);
        fileService = new FileService(fileRepository);
    }

    @Test
    void getFilesShouldReturnListOfFileNames() {
        FileJpa f1 = FileJpa.builder().originalFilename("file1.txt").build();
        FileJpa f2 = FileJpa.builder().originalFilename("file2.txt").build();

        when(fileRepository.findAll()).thenReturn(List.of(f1, f2));

        List<String> fileNames = fileService.getFiles();

        assertThat(fileNames).containsExactly("file1.txt", "file2.txt");
        verify(fileRepository, times(1)).findAll();
    }
}
