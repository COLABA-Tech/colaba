package com.example.colaba.task.infrastructure.event;

import com.example.colaba.shared.common.application.events.DomainEvent;
import com.example.colaba.shared.common.application.events.EventTypes;
import com.example.colaba.shared.common.domain.events.ProjectEvents.ProjectDeletedEvent;
import com.example.colaba.shared.common.domain.events.TagEvents.TagDeletedEvent;
import com.example.colaba.shared.common.domain.events.UserEvents.UserDeletedEvent;
import com.example.colaba.shared.common.infrastructure.rabbit.RabbitMQProperties;
import com.example.colaba.task.infrastructure.service.TaskServiceFacade;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventListener {

    private final TaskServiceFacade taskService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitMQProperties.TASK_EVENTS_QUEUE)
    public void handleEvent(Message message) {
        byte[] body = message.getBody();

        try {
            DomainEvent<JsonNode> genericEvent = objectMapper.readValue(
                    body,
                    new TypeReference<>() {
                    }
            );

            String eventType = genericEvent.getEventType();
            JsonNode payload = genericEvent.getPayload();

            switch (eventType) {
                case EventTypes.USER_DELETED -> {
                    UserDeletedEvent event = objectMapper.treeToValue(payload, UserDeletedEvent.class);
                    taskService.handleUserDeletion(event.userId());
                    log.info("Handled UserDeletedEvent for userId={}", event.userId());
                }
                case EventTypes.PROJECT_DELETED -> {
                    ProjectDeletedEvent event = objectMapper.treeToValue(payload, ProjectDeletedEvent.class);
                    taskService.handleProjectDeletion(event.projectId());
                    log.info("Handled ProjectDeletedEvent for projectId={}", event.projectId());
                }
                case EventTypes.TAG_DELETED -> {
                    TagDeletedEvent event = objectMapper.treeToValue(payload, TagDeletedEvent.class);
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