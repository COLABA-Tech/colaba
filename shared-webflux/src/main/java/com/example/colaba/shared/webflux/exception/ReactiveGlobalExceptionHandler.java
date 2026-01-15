package com.example.colaba.shared.webflux.exception;

import com.example.colaba.shared.common.dto.common.ErrorResponseDto;
import com.example.colaba.shared.common.exception.common.DuplicateEntityException;
import com.example.colaba.shared.common.exception.common.NotFoundException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ReactiveGlobalExceptionHandler {

    @ExceptionHandler(CallNotPermittedException.class)
    public Mono<ResponseEntity<ErrorResponseDto>> handleCircuitBreakerOpen(CallNotPermittedException ex, ServerWebExchange exchange) {
        log.warn("Circuit Breaker OPEN: {}", ex.getMessage());
        ErrorResponseDto dto = ErrorResponseDto.builder()
                .error("ServiceUnavailable")
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .message("The service is unavailable")
                .path(exchange.getRequest().getPath().value())
                .timestamp(OffsetDateTime.now())
                .build();
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(dto));
    }

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

    @ExceptionHandler(DuplicateEntityException.class)
    public Mono<ResponseEntity<ErrorResponseDto>> handleDuplicate(DuplicateEntityException e, ServerWebExchange exchange) {
        log.warn("Duplicate entity: {}", e.getMessage());
        ErrorResponseDto dto = ErrorResponseDto.builder()
                .error("DuplicateEntity")
                .status(HttpStatus.CONFLICT.value())
                .message(e.getMessage())
                .path(exchange.getRequest().getPath().value())
                .timestamp(OffsetDateTime.now())
                .build();
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(dto));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponseDto>> handleValidation(WebExchangeBindException e, ServerWebExchange exchange) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : e.getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        ErrorResponseDto dto = ErrorResponseDto.builder()
                .error("ValidationError")
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Invalid input")
                .path(exchange.getRequest().getPath().value())
                .timestamp(OffsetDateTime.now())
                .details(errors)
                .build();
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(dto));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ErrorResponseDto>> handleIllegalArgument(IllegalArgumentException e, ServerWebExchange exchange) {
        log.warn("Illegal argument: {}", e.getMessage());
        ErrorResponseDto dto = ErrorResponseDto.builder()
                .error("BadRequest")
                .status(HttpStatus.BAD_REQUEST.value())
                .message(e.getMessage())
                .path(exchange.getRequest().getPath().value())
                .timestamp(OffsetDateTime.now())
                .build();
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(dto));
    }

    @ExceptionHandler(WebClientResponseException.class)
    public Mono<ResponseEntity<ErrorResponseDto>> handleWebClientException(WebClientResponseException e, ServerWebExchange exchange) {
        log.error("WebClient error: {}", e.getMessage());
        int statusCode = e.getStatusCode().value();
        ErrorResponseDto dto = ErrorResponseDto.builder()
                .error("ServiceError")
                .status(statusCode)
                .message("Error calling external service")
                .path(exchange.getRequest().getPath().value())
                .timestamp(OffsetDateTime.now())
                .build();
        return Mono.just(ResponseEntity.status(statusCode).body(dto));
    }

    @ExceptionHandler(AuthenticationException.class)
    public Mono<ResponseEntity<ErrorResponseDto>> handleAuthenticationException(AuthenticationException e, ServerWebExchange exchange) {
        log.error("Authentication failed: {}", e.getMessage());
        ErrorResponseDto dto = ErrorResponseDto.builder()
                .error("Authentication failed")
                .status(HttpStatus.UNAUTHORIZED.value())
                .message("Invalid username or password")
                .path(exchange.getRequest().getPath().value())
                .timestamp(OffsetDateTime.now())
                .build();
        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(dto));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponseDto>> handleUserNotFound(UsernameNotFoundException e, ServerWebExchange exchange) {
        log.error("Username not found: {}", e.getMessage());
        ErrorResponseDto dto = ErrorResponseDto.builder()
                .error("Invalid credentials")
                .status(HttpStatus.UNAUTHORIZED.value())
                .message("Invalid username or password")
                .path(exchange.getRequest().getPath().value())
                .timestamp(OffsetDateTime.now())
                .build();
        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(dto));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public Mono<ResponseEntity<ErrorResponseDto>> handleBadCredentials(BadCredentialsException e, ServerWebExchange exchange) {
        log.error("Bad credentials: {}", e.getMessage());
        ErrorResponseDto dto = ErrorResponseDto.builder()
                .error("Invalid credentials")
                .status(HttpStatus.UNAUTHORIZED.value())
                .message("Invalid username or password")
                .path(exchange.getRequest().getPath().value())
                .timestamp(OffsetDateTime.now())
                .build();
        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(dto));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponseDto>> handleGeneralException(Exception e, ServerWebExchange exchange) {
        log.error("Unexpected error", e);
        ErrorResponseDto dto = ErrorResponseDto.builder()
                .error("InternalError")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("An unexpected error occurred")
                .path(exchange.getRequest().getPath().value())
                .timestamp(OffsetDateTime.now())
                .build();
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(dto));
    }
}