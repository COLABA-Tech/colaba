package com.example.colaba.task.infrastructure.config;

import com.example.colaba.shared.webmvc.application.ports.ProjectServicePort;
import com.example.colaba.shared.webmvc.application.ports.UserServicePort;
import com.example.colaba.task.application.mapper.CommentMapper;
import com.example.colaba.task.application.mapper.TaskMapper;
import com.example.colaba.task.application.ports.CommentRepositoryPort;
import com.example.colaba.task.application.ports.TaskRepositoryPort;
import com.example.colaba.task.application.ports.TaskTagRepositoryPort;
import com.example.colaba.task.application.service.CommentService;
import com.example.colaba.task.application.service.TaskService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommentServiceConfig {

    @Bean
    public CommentMapper commentMapper() {
        return new CommentMapper();
    }

    @Bean
    public CommentService commentService(
            CommentRepositoryPort commentRepository,
            TaskRepositoryPort taskRepository,
            UserServicePort userService,
            CommentMapper commentMapper
    ) {
        return new CommentService(
                commentRepository,
                taskRepository,
                userService,
                commentMapper
        );
    }

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