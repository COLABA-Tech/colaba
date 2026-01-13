package com.example.colaba.shared.webflux.exception;

import com.example.colaba.shared.common.dto.common.ErrorResponseDto;
import com.example.colaba.shared.common.exception.common.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@Slf4j
@RestControllerAdvice
public class ReactiveGlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    public Mono<ResponseEntity<ErrorResponseDto>> handleNotFound(NotFoundException e, ServerWebExchange exchange) {
        log.warn("Resource not found: {}", e.getMessage());
        ErrorResponseDto dto = ErrorResponseDto.builder()
                .error("NotFound")
                .status(HttpStatus.NOT_FOUND.value())
                .message(e.getMessage())
                .path(exchange.getRequest().getPath().value())
                .timestamp(OffsetDateTime.now())
                .build();
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(dto));
    }
}