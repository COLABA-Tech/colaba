package com.example.colaba.task.listener;

import com.example.colaba.shared.common.events.ProjectEvents.ProjectDeletedEvent;
import com.example.colaba.shared.common.events.TagEvents;
import com.example.colaba.shared.common.events.UserEvents;
import com.example.colaba.shared.common.rabbit.DomainEvent;
import com.example.colaba.shared.common.rabbit.RabbitMQProperties;
import com.example.colaba.task.service.TaskService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventListener {

    private final TaskService taskService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitMQProperties.TASK_EVENTS_QUEUE)
    public void handleEvent(Message message) {
        byte[] body = message.getBody();

        try {
            DomainEvent<JsonNode> genericEvent = objectMapper.readValue(
                    body,
                    new TypeReference<DomainEvent<JsonNode>>() {}
            );

            String eventType = genericEvent.getEventType();
            JsonNode payload = genericEvent.getPayload();

            switch (eventType) {
                case "user.deleted" -> {
                    UserEvents.UserDeletedEvent event = objectMapper.treeToValue(payload, UserEvents.UserDeletedEvent.class);
                    taskService.handleUserDeletion(event.userId());
                    log.info("Handled UserDeletedEvent for userId={}", event.userId());
                }
                case "project.deleted" -> {
                    ProjectDeletedEvent event = objectMapper.treeToValue(payload, ProjectDeletedEvent.class);
                    taskService.handleProjectDeletion(event.projectId());
                    log.info("Handled ProjectDeletedEvent for projectId={}", event.projectId());
                }
                case "tag.deleted" -> {
                    TagEvents.TagDeletedEvent event = objectMapper.treeToValue(payload, TagEvents.TagDeletedEvent.class);
                    taskService.handleTagDeletion(event.tagId());
                    log.info("Handled TagDeletedEvent for tagId={}", event.tagId());
                }
                default -> log.warn("Ignored unsupported event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Failed to process event from RabbitMQ", e);
            throw new RuntimeException("Event processing failed", e);
        }
    }
}