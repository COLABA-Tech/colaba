package com.example.colaba.shared.webmvc.infrastructure.feign;

import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;

@Slf4j
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor(@Value("${internal.api-key}") String internalApiKey) {
        return template -> {
            boolean isMultipartRequest = template.headers().containsKey("Content-Type") &&
                    template.headers().get("Content-Type").stream()
                            .anyMatch(header -> header.contains(MediaType.MULTIPART_FORM_DATA_VALUE));

            if (!isMultipartRequest) {
                template.header("Content-Type", MediaType.APPLICATION_JSON_VALUE);
            }

            template.header("Accept", MediaType.APPLICATION_JSON_VALUE);
            template.header("X-Internal-Key", internalApiKey);
        };
    }
}