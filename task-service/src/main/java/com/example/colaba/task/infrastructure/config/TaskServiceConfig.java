package com.example.colaba.task.infrastructure.config;

import com.example.colaba.shared.webmvc.application.ports.ProjectServicePort;
import com.example.colaba.shared.webmvc.application.ports.UserServicePort;
import com.example.colaba.task.application.mapper.TaskMapper;
import com.example.colaba.task.application.ports.CommentRepositoryPort;
import com.example.colaba.task.application.ports.TaskRepositoryPort;
import com.example.colaba.task.application.ports.TaskTagRepositoryPort;
import com.example.colaba.task.application.service.TaskService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TaskServiceConfig {

    @Bean
    public TaskService taskService(
            TaskRepositoryPort taskRepository,
            TaskTagRepositoryPort taskTagRepository,
            CommentRepositoryPort commentRepository,
            ProjectServicePort projectService,
            UserServicePort userService
    ) {
        return new TaskService(
                taskRepository,
                taskTagRepository,
                commentRepository,
                projectService,
                userService,
                new TaskMapper()
        );
    }
}
