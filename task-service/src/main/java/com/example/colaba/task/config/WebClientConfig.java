package com.example.colaba.task.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${internal.api-key}")
    private String internalApiKey;

    @Bean(name = "fileWebClient")
    public WebClient fileWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("http://file-service")
                .defaultHeader("X-Internal-Key", internalApiKey)
                .build();
    }
}