package com.example.colaba.shared.webflux.rabbit;

import com.rabbitmq.client.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.rabbitmq.*;

@Configuration
public class RabbitMQConfigReactive {

    @Bean
    public Sender sender(ConnectionFactory connectionFactory) {
        return RabbitFlux.createSender(new SenderOptions().connectionFactory(connectionFactory));
    }

    @Bean
    public Receiver receiver(ConnectionFactory connectionFactory) {
        return RabbitFlux.createReceiver(new ReceiverOptions().connectionFactory(connectionFactory));
    }
}