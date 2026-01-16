package com.example.colaba.shared.webmvc.feign;

import com.example.colaba.shared.common.exception.common.DuplicateEntityException;
import com.example.colaba.shared.common.exception.common.NotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.naming.ServiceUnavailableException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("Feign error - Status: {}, Method: {}, URL: {}",
                response.status(), methodKey, response.request().url());

        if (response.status() == 404) {
            String message = extractErrorMessage(response);
            return new NotFoundException(message != null ? message : "Resource not found");
        }

        if (response.status() == 409) {
            String message = extractErrorMessage(response);
            return new DuplicateEntityException(message != null ? message : "Duplicate entity");
        }

        if (response.status() == 400) {
            String message = extractErrorMessage(response);
            return new IllegalArgumentException(message != null ? message : "Bad request");
        }

        if (response.status() == 503) {
            String message = extractErrorMessage(response);
            return new ServiceUnavailableException(message != null ? message : "Service temporarily unavailable");
        }

        return defaultErrorDecoder.decode(methodKey, response);
    }

    private String extractErrorMessage(Response response) {
        try {
            if (response.body() != null) {
                String body = Util.toString(response.body().asReader(StandardCharsets.UTF_8));

                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(body);

                if (jsonNode.has("message")) {
                    return jsonNode.get("message").asText();
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract error message from response", e);
        }
        return null;
    }
}