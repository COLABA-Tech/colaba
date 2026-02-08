package com.example.colaba.project.listener;

import com.example.colaba.project.service.ProjectService;
import com.example.colaba.shared.common.events.UserEvents.UserDeletedEvent;
import com.example.colaba.shared.common.rabbit.DomainEvent;
import com.example.colaba.shared.common.rabbit.RabbitMQProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.Receiver;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventConsumer {

    private final Receiver receiver;
    private final ProjectService projectService;
    private final ObjectMapper objectMapper;

    @EventListener(ApplicationReadyEvent.class)
    public void startConsuming() {
        receiver.consumeAutoAck(RabbitMQProperties.PROJECT_EVENTS_QUEUE)
                .flatMap(delivery -> {
                    try {
                        DomainEvent<UserDeletedEvent> event = objectMapper.readValue(
                                delivery.getBody(),
                                new TypeReference<>() {
                                }
                        );

                        if ("user.deleted".equals(event.getEventType())) {
                            Long userId = event.getPayload().userId();
                            return projectService.handleUserDeletion(userId)
                                    .doOnSuccess(v -> log.info("ProjectService handled UserDeletedEvent for userId={}", userId))
                                    .onErrorResume(e -> {
                                        log.error("Error handling UserDeletedEvent for userId={}", userId, e);
                                        return Mono.empty();
                                    });
                        }
                        return Mono.empty();
                    } catch (Exception e) {
                        log.error("Failed to deserialize or process user event", e);
                        return Mono.empty();
                    }
                })
                .subscribe();
    }
}
