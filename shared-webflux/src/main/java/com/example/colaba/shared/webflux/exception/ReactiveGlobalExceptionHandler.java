package com.example.colaba.shared.webflux.exception;

import com.example.colaba.shared.common.dto.common.ErrorResponseDto;
import com.example.colaba.shared.common.exception.common.DuplicateEntityException;
import com.example.colaba.shared.common.exception.common.NotFoundException;
import com.example.colaba.shared.common.exception.user.UserPasswordSameAsOldException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.jsonwebtoken.io.DecodingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @ExceptionHandler(AccessDeniedException.class)
    public Mono<ResponseEntity<ErrorResponseDto>> handleAccessDenied(AccessDeniedException e, ServerWebExchange exchange) {
        log.warn("Access denied: {}", e.getMessage());
        ErrorResponseDto dto = ErrorResponseDto.builder()
                .error("AccessDenied")
                .status(HttpStatus.FORBIDDEN.value())
                .message(e.getMessage())
                .path(exchange.getRequest().getPath().value())
                .timestamp(OffsetDateTime.now())
                .build();
        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body(dto));
    }

    @ExceptionHandler({ServerWebInputException.class, DecodingException.class})
    public Mono<ResponseEntity<ErrorResponseDto>> handleDecodingError(
            Throwable ex,
            ServerWebExchange exchange) {

        log.warn("Decoding/input error: {}", ex.getMessage());

        String message = "Invalid value for enum field";
        Map<String, Object> details = new HashMap<>();

        Throwable cause = (ex.getCause() != null) ? ex.getCause() : ex;

        if (cause instanceof InvalidFormatException ife) {
            Class<?> targetType = ife.getTargetType();
            String field = getFieldName(ife);

            if (targetType.isEnum()) {
                String enumName = targetType.getSimpleName();
                String invalidValue = String.valueOf(ife.getValue());
                List<String> allowed = Arrays.stream(targetType.getEnumConstants())
                        .map(Object::toString)
                        .toList();

                details.put("field", field);
                details.put("invalidValue", invalidValue);
                details.put("enumType", enumName);
                details.put("allowedValues", allowed);
            } else {
                // Для других типов можно добавить позже
                details.put("field", field);
                details.put("invalidValue", ife.getValue());
                details.put("targetType", targetType.getSimpleName());
            }
        } else {
            message = "Failed to parse request body";
            details.put("reason", cause.getMessage());
        }

        ErrorResponseDto dto = ErrorResponseDto.builder()
                .error("InvalidFormat")
                .status(HttpStatus.BAD_REQUEST.value())
                .message(message)
                .path(exchange.getRequest().getPath().value())
                .timestamp(OffsetDateTime.now())
                .details(details.isEmpty() ? null : details)
                .build();

        return Mono.just(ResponseEntity.badRequest().body(dto));
    }

    private String getFieldName(InvalidFormatException ex) {
        if (!ex.getPath().isEmpty()) {
            JsonMappingException.Reference ref = ex.getPath().get(ex.getPath().size() - 1);
            return ref.getFieldName() != null ? ref.getFieldName() : "unknown";
        }
        return "unknown";
    }

    @ExceptionHandler(UserPasswordSameAsOldException.class)
    public Mono<ResponseEntity<ErrorResponseDto>> handlePasswordSameAsOld(
            UserPasswordSameAsOldException ex,
            ServerWebExchange exchange) {

        log.warn("Attempt to set same password: {}", ex.getMessage());

        ErrorResponseDto dto = ErrorResponseDto.builder()
                .error("BadRequest")
                .status(HttpStatus.BAD_REQUEST.value())
                .message("New password cannot be the same as the current one")
                .path(exchange.getRequest().getPath().value())
                .timestamp(OffsetDateTime.now())
                .build();

        return Mono.just(ResponseEntity.badRequest().body(dto));
    }

    @ExceptionHandler(MethodNotAllowedException.class)
    public Mono<ResponseEntity<ErrorResponseDto>> handleMethodNotAllowed(
            MethodNotAllowedException ex,
            ServerWebExchange exchange) {

        String allowed = ex.getSupportedMethods().stream()
                .map(HttpMethod::name)
                .collect(Collectors.joining(", "));

        ErrorResponseDto dto = ErrorResponseDto.builder()
                .error("MethodNotAllowed")
                .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                .message(String.format("Method %s is not allowed. Allowed methods: %s",
                        ex.getHttpMethod(), allowed))
                .path(exchange.getRequest().getPath().value())
                .timestamp(OffsetDateTime.now())
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(dto));
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