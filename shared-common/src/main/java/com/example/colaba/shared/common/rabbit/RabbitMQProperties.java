package com.example.colaba.shared.common.rabbit;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "rabbitmq")
public class RabbitMQProperties {
    private final String notificationsExchange = "notifications-exchange";
    private final String userEventsQueue = "user-events-queue";
    private final String projectEventsQueue = "project-events-queue";
    private final String taskEventsQueue = "task-events-queue";
    private final String tagEventsQueue = "tag-events-queue";
}