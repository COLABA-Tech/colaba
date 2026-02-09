package com.example.colaba.shared.webmvc.infrastructure.rabbit;

import com.example.colaba.shared.common.application.events.DomainEvent;
import com.example.colaba.shared.common.infrastructure.rabbit.RabbitMQProperties;
import com.example.colaba.shared.webmvc.application.ports.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitEventPublisher implements EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publish(DomainEvent<?> event) {
        rabbitTemplate.convertAndSend(
                RabbitMQProperties.NOTIFICATIONS_EXCHANGE,
                event.getEventType(),
                event
        );
        log.info("Published event: {} from {}", event.getEventType(), event.getSourceService());
    }
}
