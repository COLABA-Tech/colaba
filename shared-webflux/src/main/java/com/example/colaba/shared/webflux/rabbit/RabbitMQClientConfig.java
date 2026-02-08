package com.example.colaba.shared.webflux.rabbit;

import com.rabbitmq.client.ConnectionFactory;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQClientConfig {

    @Bean
    public ConnectionFactory rabbitClientConnectionFactory(RabbitProperties properties) {
        ConnectionFactory factory = new ConnectionFactory();

        factory.setHost(properties.getHost());
        factory.setPort(properties.getPort());
        factory.setUsername(properties.getUsername());
        factory.setPassword(properties.getPassword());
        factory.setVirtualHost(properties.getVirtualHost());

        factory.setVirtualHost(
                properties.getVirtualHost() != null
                        ? properties.getVirtualHost()
                        : "/"
        );

        return factory;
    }
}
