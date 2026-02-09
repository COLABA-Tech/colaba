package com.example.colaba.task.infrastructure.config;

import com.example.colaba.shared.webmvc.application.ports.UserServicePort;
import com.example.colaba.shared.webmvc.application.security.ProjectAccessService;
import com.example.colaba.task.application.service.CommentService;
import com.example.colaba.task.application.service.CommentServicePublic;
import com.example.colaba.task.application.service.TaskService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommentServicePublicConfig {

    @Bean
    public CommentServicePublic commentServicePublic(
            CommentService commentService,
            TaskService taskService,
            ProjectAccessService projectAccessService,
            UserServicePort userService
    ) {
        return new CommentServicePublic(
                commentService,
                taskService,
                projectAccessService,
                userService
        );
    }
}