package com.example.colaba.shared.webmvc.exception;

import com.example.colaba.shared.common.dto.common.ErrorResponseDto;
import com.example.colaba.shared.common.exception.common.DuplicateEntityException;
import com.example.colaba.shared.common.exception.common.NotFoundException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<ErrorResponseDto> handleCircuitBreakerOpen(CallNotPermittedException ex, HttpServletRequest request) {
        log.warn("Circuit Breaker OPEN: {}", ex.getMessage());
        ErrorResponseDto dto = ErrorResponseDto.builder()
                .error("NotFound")
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .message("The service is unavailable")
                .path(request.getRequestURI())
                .timestamp(OffsetDateTime.now())
                .build();
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(dto);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNotFound(NotFoundException e, HttpServletRequest request) {
        log.warn("Resource not found: {}", e.getMessage());
        ErrorResponseDto dto = ErrorResponseDto.builder()
                .error("NotFound")
                .status(HttpStatus.NOT_FOUND.value())
                .message(e.getMessage())
                .path(request.getRequestURI())
                .timestamp(OffsetDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(dto);
    }

    @ExceptionHandler(DuplicateEntityException.class)
    public ResponseEntity<ErrorResponseDto> handleDuplicate(DuplicateEntityException e, HttpServletRequest request) {
        log.warn("Duplicate entity: {}", e.getMessage());
        ErrorResponseDto dto = ErrorResponseDto.builder()
                .error("DuplicateEntity")
                .status(HttpStatus.CONFLICT.value())
                .message(e.getMessage())
                .path(request.getRequestURI())
                .timestamp(OffsetDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(dto);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidation(MethodArgumentNotValidException e, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        ErrorResponseDto dto = ErrorResponseDto.builder()
                .error("ValidationError")
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Invalid input")
                .path(request.getRequestURI())
                .timestamp(OffsetDateTime.now())
                .details(errors)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(dto);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgument(IllegalArgumentException e, HttpServletRequest request) {
        log.warn("Illegal argument: {}", e.getMessage());
        ErrorResponseDto dto = ErrorResponseDto.builder()
                .error("BadRequest")
                .status(HttpStatus.BAD_REQUEST.value())
                .message(e.getMessage())
                .path(request.getRequestURI())
                .timestamp(OffsetDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(dto);
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponseDto> handleFeignException(FeignException e, HttpServletRequest request) {
        log.error("Feign client error: {}", e.getMessage());
        int statusCode = e.status() >= 100 && e.status() <= 599 ? e.status() : 500;
        ErrorResponseDto dto = ErrorResponseDto.builder()
                .error("ServiceError")
                .status(statusCode)
                .message("Error calling external service")
                .path(request.getRequestURI())
                .timestamp(OffsetDateTime.now())
                .build();
        return ResponseEntity.status(statusCode).body(dto);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNoResourceFound(
            NoResourceFoundException e, HttpServletRequest request) {
        String path = e.getResourcePath();
        if (path.contains("/v3/api-docs") || path.contains("/swagger-ui") || path.equals("/favicon.ico")) {
            log.debug("Resource not found (expected for Swagger): {}", path);
            return null;
        }
        log.warn("Resource not found: {}", path);
        ErrorResponseDto dto = ErrorResponseDto.builder()
                .error("NoResourceFoundException")
                .status(HttpStatus.NOT_FOUND.value())
                .message("Resource not found: " + path)
                .path(request.getRequestURI())
                .timestamp(OffsetDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(dto);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDenied(AccessDeniedException e,
                                                               HttpServletRequest request) {
        log.warn("Access denied: {}", e.getMessage());
        ErrorResponseDto dto = ErrorResponseDto.builder()
                .error("AccessDenied")
                .status(HttpStatus.FORBIDDEN.value())
                .message(e.getMessage())
                .path(request.getRequestURI())
                .timestamp(OffsetDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(dto);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDto> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        log.error("Authentication failed: {}", e.getMessage());
        ErrorResponseDto dto = ErrorResponseDto.builder()
                .error("Authentication failed")
                .status(HttpStatus.UNAUTHORIZED.value())
                .message("Invalid username or password")
                .path(request.getRequestURI())
                .timestamp(OffsetDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(dto);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleUserNotFound(UsernameNotFoundException e, HttpServletRequest request) {
        log.error("Username not found: {}", e.getMessage());
        ErrorResponseDto dto = ErrorResponseDto.builder()
                .error("Invalid credentials")
                .status(HttpStatus.UNAUTHORIZED.value())
                .message("Invalid username or password")
                .path(request.getRequestURI())
                .timestamp(OffsetDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(dto);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handleBadCredentials(BadCredentialsException e, HttpServletRequest request) {
        log.error("Bad credentials: {}", e.getMessage());
        ErrorResponseDto dto = ErrorResponseDto.builder()
                .error("Invalid credentials")
                .status(HttpStatus.UNAUTHORIZED.value())
                .message("Invalid username or password")
                .path(request.getRequestURI())
                .timestamp(OffsetDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(dto);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGeneralException(Exception e, HttpServletRequest request) {
        log.error("Unexpected error", e);
        ErrorResponseDto dto = ErrorResponseDto.builder()
                .error("InternalError")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("An unexpected error occurred")
                .path(request.getRequestURI())
                .timestamp(OffsetDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(dto);
    }
}