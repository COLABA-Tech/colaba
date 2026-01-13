package com.example.colaba.shared.common.feign;

import feign.Logger;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

@Slf4j
public class FeignConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }

    @Bean
    public RequestInterceptor requestInterceptor(@Value("${internal.api-key}") String internalApiKey) {
        log.info("Feign Config - Internal API Key configured: {}",
                internalApiKey != null ? "SET" : "NOT SET");

        return template -> {
            template.header("Content-Type", "application/json");
            template.header("Accept", "application/json");
            template.header("X-Internal-Key", internalApiKey);
        };
    }
}