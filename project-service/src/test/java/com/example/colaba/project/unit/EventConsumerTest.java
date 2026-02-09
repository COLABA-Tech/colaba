package com.example.colaba.project.unit;

import com.example.colaba.project.listener.EventConsumer;
import com.example.colaba.project.service.ProjectService;
import com.example.colaba.shared.common.application.events.DomainEvent;
import com.example.colaba.shared.common.domain.events.UserEvents;
import com.example.colaba.shared.common.infrastructure.rabbit.RabbitMQProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.AcknowledgableDelivery;
import reactor.rabbitmq.Receiver;
import reactor.test.publisher.PublisherProbe;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventConsumerTest {

    @Mock
    private Receiver receiver;

    @Mock
    private ProjectService projectService;

    @Mock
    private ObjectMapper objectMapper;

    private EventConsumer eventConsumer;
    private PublisherProbe<AcknowledgableDelivery> publisherProbe;

    private static final String SOURCE_SERVICE = "user-service";

    @BeforeEach
    void setUp() {
        eventConsumer = new EventConsumer(receiver, projectService, objectMapper);
        publisherProbe = PublisherProbe.empty();
    }

    @Test
    void startConsuming_shouldProcessUserDeletedEvent() throws Exception {
        // Arrange
        Long userId = 123L;

        UserEvents.UserDeletedEvent userDeletedEvent = new UserEvents.UserDeletedEvent(userId);
        DomainEvent<UserEvents.UserDeletedEvent> domainEvent = new DomainEvent<>(
                "user.deleted",
                userDeletedEvent,
                SOURCE_SERVICE
        );

        String eventJson = String.format(
                "{\"eventType\":\"user.deleted\",\"payload\":{\"userId\":%d},\"sourceService\":\"%s\"}",
                userId, SOURCE_SERVICE
        );
        byte[] eventBytes = eventJson.getBytes(StandardCharsets.UTF_8);

        AcknowledgableDelivery delivery = mock(AcknowledgableDelivery.class);
        when(delivery.getBody()).thenReturn(eventBytes);

        when(receiver.consumeAutoAck(RabbitMQProperties.PROJECT_EVENTS_QUEUE))
                .thenReturn(Flux.just(delivery));

        when(objectMapper.readValue(eq(eventBytes), any(TypeReference.class)))
                .thenReturn(domainEvent);

        when(projectService.handleUserDeletion(userId))
                .thenReturn(Mono.empty());

        // Act
        eventConsumer.startConsuming();

        // Assert
        verify(projectService, timeout(1000).times(1)).handleUserDeletion(userId);
    }

    @Test
    void startConsuming_shouldIgnoreUnknownEventType() throws Exception {
        // Arrange
        DomainEvent<UserEvents.UserDeletedEvent> domainEvent = new DomainEvent<>(
                "user.updated",
                new UserEvents.UserDeletedEvent(123L),
                SOURCE_SERVICE
        );

        String eventJson = String.format(
                "{\"eventType\":\"user.updated\",\"payload\":{\"userId\":123},\"sourceService\":\"%s\"}",
                SOURCE_SERVICE
        );
        byte[] eventBytes = eventJson.getBytes(StandardCharsets.UTF_8);

        AcknowledgableDelivery delivery = mock(AcknowledgableDelivery.class);
        when(delivery.getBody()).thenReturn(eventBytes);

        when(receiver.consumeAutoAck(RabbitMQProperties.PROJECT_EVENTS_QUEUE))
                .thenReturn(Flux.just(delivery));

        when(objectMapper.readValue(eq(eventBytes), any(TypeReference.class)))
                .thenReturn(domainEvent);

        // Act
        eventConsumer.startConsuming();

        // Assert
        verify(projectService, after(1000).never()).handleUserDeletion(anyLong());
    }

    @Test
    void startConsuming_shouldLogErrorWhenServiceThrowsException() throws Exception {
        // Arrange
        Long userId = 123L;

        UserEvents.UserDeletedEvent userDeletedEvent = new UserEvents.UserDeletedEvent(userId);
        DomainEvent<UserEvents.UserDeletedEvent> domainEvent = new DomainEvent<>(
                "user.deleted",
                userDeletedEvent,
                SOURCE_SERVICE
        );

        String eventJson = String.format(
                "{\"eventType\":\"user.deleted\",\"payload\":{\"userId\":%d},\"sourceService\":\"%s\"}",
                userId, SOURCE_SERVICE
        );
        byte[] eventBytes = eventJson.getBytes(StandardCharsets.UTF_8);

        AcknowledgableDelivery delivery = mock(AcknowledgableDelivery.class);
        when(delivery.getBody()).thenReturn(eventBytes);

        when(receiver.consumeAutoAck(RabbitMQProperties.PROJECT_EVENTS_QUEUE))
                .thenReturn(Flux.just(delivery));

        when(objectMapper.readValue(eq(eventBytes), any(TypeReference.class)))
                .thenReturn(domainEvent);

        RuntimeException serviceException = new RuntimeException("Service unavailable");
        when(projectService.handleUserDeletion(userId))
                .thenReturn(Mono.error(serviceException));

        // Act
        eventConsumer.startConsuming();

        // Assert
        verify(projectService, timeout(1000).times(1)).handleUserDeletion(userId);
        // Здесь можно проверить лог, если используете моки для логгера
    }

    @Test
    void startConsuming_shouldLogErrorWhenDeserializationFails() throws Exception {
        // Arrange
        String invalidJson = "{invalid json}";
        byte[] invalidBytes = invalidJson.getBytes(StandardCharsets.UTF_8);

        AcknowledgableDelivery delivery = mock(AcknowledgableDelivery.class);
        when(delivery.getBody()).thenReturn(invalidBytes);

        when(receiver.consumeAutoAck(RabbitMQProperties.PROJECT_EVENTS_QUEUE))
                .thenReturn(Flux.just(delivery));

        Exception jsonException = new RuntimeException("Invalid JSON");
        when(objectMapper.readValue(eq(invalidBytes), any(TypeReference.class)))
                .thenThrow(jsonException);

        // Act
        eventConsumer.startConsuming();

        // Assert
        verify(projectService, after(1000).never()).handleUserDeletion(anyLong());
        // Здесь можно проверить лог об ошибке десериализации
    }
}