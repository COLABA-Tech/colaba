package com.example.colaba.shared.common.infrastructure.rabbit;

import com.example.colaba.shared.common.application.events.EventTypes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RabbitMQConfigCommon {

    @Bean
    public TopicExchange notificationsExchange() {
        return ExchangeBuilder
                .topicExchange(RabbitMQProperties.NOTIFICATIONS_EXCHANGE)
                .durable(true)
                .build();
    }

    private Queue createQuorumQueue(String name) {
        Map<String, Object> args = new HashMap<>();
        args.put("x-queue-type", "quorum");
        args.put("x-quorum-initial-group-size", 3);
        return QueueBuilder
                .durable(name)
                .withArguments(args)
                .build();
    }

    @Bean
    public Queue userEventsQueue() {
        return createQuorumQueue(RabbitMQProperties.USER_EVENTS_QUEUE);
    }

    @Bean
    public Queue projectEventsQueue() {
        return createQuorumQueue(RabbitMQProperties.PROJECT_EVENTS_QUEUE);
    }

    @Bean
    public Queue taskEventsQueue() {
        return createQuorumQueue(RabbitMQProperties.TASK_EVENTS_QUEUE);
    }

    @Bean
    public Binding projectEventsUserDeletedBinding() {
        return BindingBuilder
                .bind(projectEventsQueue())
                .to(notificationsExchange())
                .with(EventTypes.USER_DELETED);
    }

    @Bean
    public Binding taskEventsUserDeletedBinding() {
        return BindingBuilder
                .bind(taskEventsQueue())
                .to(notificationsExchange())
                .with(EventTypes.USER_DELETED);
    }

    @Bean
    public Binding taskEventsProjectDeletedBinding() {
        return BindingBuilder
                .bind(taskEventsQueue())
                .to(notificationsExchange())
                .with(EventTypes.PROJECT_DELETED);
    }

    @Bean
    public Binding taskEventsTagDeletedBinding() {
        return BindingBuilder
                .bind(taskEventsQueue())
                .to(notificationsExchange())
                .with(EventTypes.TAG_DELETED);
    }
}
