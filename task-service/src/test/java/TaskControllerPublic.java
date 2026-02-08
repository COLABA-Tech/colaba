import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerPublic {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "1", roles = "USER")
    void createTask_userIsProjectMember_success() throws Exception {
        mockMvc.perform(
                post("/api/public/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "title": "Public task",
                              "description": "desc",
                              "projectId": 1,
                              "assigneeId": 1,
                              "priority": "MEDIUM"
                            }
                        """)
        )
        .andExpect(status().isCreated());
    }
}
