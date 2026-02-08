package com.example.colaba.shared.webflux.rabbit;

import com.example.colaba.shared.common.events.ProjectEvents.ProjectDeletedEvent;
import com.example.colaba.shared.common.events.TagEvents.TagDeletedEvent;
import com.example.colaba.shared.common.events.TaskEvents.TaskDeletedEvent;
import com.example.colaba.shared.common.events.UserEvents.UserDeletedEvent;
import com.example.colaba.shared.common.rabbit.DomainEvent;
import com.example.colaba.shared.common.rabbit.RabbitMQProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Sender;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisherReactive {

    private final Sender sender;
    private final ObjectMapper objectMapper;
    private final RabbitMQProperties properties;

    public Mono<Void> publishUserDeleted(UserDeletedEvent event) {
        return publish("user.deleted", event, "user-service");
    }

    public Mono<Void> publishProjectDeleted(ProjectDeletedEvent event) {
        return publish("project.deleted", event, "project-service");
    }

    public Mono<Void> publishTaskDeleted(TaskDeletedEvent event) {
        return publish("task.deleted", event, "task-service");
    }

    public Mono<Void> publishTagDeleted(TagDeletedEvent event) {
        return publish("tag.deleted", event, "project-service");
    }

    private <T> Mono<Void> publish(String routingKey, T payload, String source) {
        return Mono.fromCallable(() -> {
                    DomainEvent<T> domainEvent = new DomainEvent<>(routingKey, payload, source);
                    return objectMapper.writeValueAsBytes(domainEvent);
                })
                .flatMap(bytes -> {
                    OutboundMessage message = new OutboundMessage(RabbitMQProperties.NOTIFICATIONS_EXCHANGE, routingKey, bytes);
                    return sender.send(Mono.just(message));
                })
                .doOnSuccess(v -> log.info("Published event: {} from {}", routingKey, source))
                .doOnError(e -> log.error("Error publishing event: {}", routingKey, e));
    }
}