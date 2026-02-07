package com.example.colaba.shared.webmvc.rabbit;

import com.example.colaba.shared.common.events.ProjectEvents.ProjectDeletedEvent;
import com.example.colaba.shared.common.events.TagEvents.TagDeletedEvent;
import com.example.colaba.shared.common.events.TaskEvents.TaskDeletedEvent;
import com.example.colaba.shared.common.events.UserEvents.UserDeletedEvent;
import com.example.colaba.shared.common.rabbit.DomainEvent;
import com.example.colaba.shared.common.rabbit.RabbitMQProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQProperties properties;

    private <T> void publish(String routingKey, T payload, String source) {
        DomainEvent<T> domainEvent = new DomainEvent<>(routingKey, payload, source);
        rabbitTemplate.convertAndSend(properties.getNotificationsExchange(), routingKey, domainEvent);
        log.info("Published event: {} from {}", routingKey, source);
    }

    public void publishUserDeleted(UserDeletedEvent event) {
        publish("user.deleted", event, "user-service");
    }

    public void publishProjectDeleted(ProjectDeletedEvent event) {
        publish("project.deleted", event, "project-service");
    }

    public void publishTaskDeleted(TaskDeletedEvent event) {
        publish("task.deleted", event, "task-service");
    }

    public void publishTagDeleted(TagDeletedEvent event) {
        publish("tag.deleted", event, "project-service");
    }
}
