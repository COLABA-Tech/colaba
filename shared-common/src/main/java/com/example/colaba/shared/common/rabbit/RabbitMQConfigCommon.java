package com.example.colaba.shared.common.rabbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RabbitMQConfigCommon {

    private final RabbitMQProperties properties;

    @Bean
    public TopicExchange notificationsExchange() {
        return ExchangeBuilder
                .topicExchange(properties.getNotificationsExchange())
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
        return createQuorumQueue(properties.getUserEventsQueue());
    }

    @Bean
    public Queue projectEventsQueue() {
        return createQuorumQueue(properties.getProjectEventsQueue());
    }

    @Bean
    public Queue taskEventsQueue() {
        return createQuorumQueue(properties.getTaskEventsQueue());
    }

    @Bean
    public Queue tagEventsQueue() {
        return createQuorumQueue(properties.getTagEventsQueue());
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
