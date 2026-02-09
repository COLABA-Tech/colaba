package com.example.colaba.shared.common.infrastructure.rabbit;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "rabbitmq")
public class RabbitMQProperties {
    public static final String NOTIFICATIONS_EXCHANGE = "notifications-exchange";
    public static final String USER_EVENTS_QUEUE = "user-events-queue";
    public static final String PROJECT_EVENTS_QUEUE = "project-events-queue";
    public static final String TASK_EVENTS_QUEUE = "task-events-queue";
}