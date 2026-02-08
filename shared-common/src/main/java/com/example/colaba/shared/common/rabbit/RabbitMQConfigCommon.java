package com.example.colaba.shared.common.rabbit;

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
                .with("user.deleted");
    }

    @Bean
    public Binding userEventsBinding() {
        return BindingBuilder
                .bind(userEventsQueue())
                .to(notificationsExchange())
                .with("user.*");
    }

    @Bean
    public Binding projectEventsBinding() {
        return BindingBuilder
                .bind(projectEventsQueue())
                .to(notificationsExchange())
                .with("project.*");
    }

    @Bean
    public Binding taskEventsBinding() {
        return BindingBuilder
                .bind(taskEventsQueue())
                .to(notificationsExchange())
                .with("task.*");
    }

    @Bean
    public Binding tagEventsBinding() {
        return BindingBuilder
                .bind(tagEventsQueue())
                .to(notificationsExchange())
                .with("tag.*");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter(new ObjectMapper());
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        template.setMandatory(true);
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                log.error("Message nacked: {}", cause);
            }
        });
        template.setReturnsCallback(returned -> log.warn("Message returned: {}", returned.getReplyText()));
        return template;
    }
}
