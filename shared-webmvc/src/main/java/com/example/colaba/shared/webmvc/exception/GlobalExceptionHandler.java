package com.example.colaba.shared.webmvc.exception;

import com.example.colaba.shared.common.dto.common.ErrorResponseDto;
import com.example.colaba.shared.common.exception.common.DuplicateEntityException;
import com.example.colaba.shared.common.exception.common.NotFoundException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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
import java.util.Arrays;
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

        String msg = e.getMessage() != null && e.getMessage().contains("anonymous")
                ? "Authentication required. Please log in."
                : "You do not have permission to access this resource.";

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

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        log.warn("Cannot read request body: {}", ex.getMostSpecificCause().getMessage());

        String message = "Unable to read request body. Please check JSON format and Content-Type.";

        if (ex.getCause() instanceof InvalidFormatException) {
            return handleInvalidFormat((InvalidFormatException) ex.getCause(), request);
        }

        ErrorResponseDto dto = ErrorResponseDto.builder()
                .error("InvalidRequestBody")
                .status(HttpStatus.BAD_REQUEST.value())
                .message(message)
                .path(request.getRequestURI())
                .timestamp(OffsetDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(dto);
    }

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidFormat(
            InvalidFormatException ex,
            HttpServletRequest request) {

        log.warn("Invalid value format: {} → {}", ex.getValue(), ex.getTargetType().getSimpleName());

        String message;
        Map<String, Object> details = new HashMap<>();
        String field = getFieldName(ex);

        Class<?> targetType = ex.getTargetType();

        if (targetType.isEnum()) {
            String enumName = targetType.getSimpleName();
            String invalid = String.valueOf(ex.getValue());
            String allowed = String.join(", ",
                    Arrays.stream(targetType.getEnumConstants())
                            .map(Object::toString)
                            .toList());

            message = String.format(
                    "Invalid value '%s' for field '%s' (enum %s). Allowed values: %s",
                    invalid, field, enumName, allowed
            );

            details.put("field", field);
            details.put("invalidValue", invalid);
            details.put("allowedValues", allowed.split(", "));
        } else if (Number.class.isAssignableFrom(targetType) || targetType.isPrimitive()) {
            message = String.format(
                    "Invalid numeric format in field '%s': '%s' cannot be parsed as %s",
                    field, ex.getValue(), targetType.getSimpleName()
            );
            details.put("field", field);
            details.put("invalidValue", ex.getValue());
        } else if (targetType == OffsetDateTime.class ||
                targetType.getName().contains("LocalDate") ||
                targetType.getName().contains("ZonedDateTime")) {
            message = String.format(
                    "Invalid date/time format in field '%s': '%s'. Expected ISO-8601 (example: 2025-01-20T14:30:00Z)",
                    field, ex.getValue()
            );
            details.put("field", field);
            details.put("invalidValue", ex.getValue());
            details.put("expectedFormat", "ISO-8601 Offset Date-Time");
        } else {
            message = String.format(
                    "Cannot convert value '%s' to type %s (field '%s')",
                    ex.getValue(), targetType.getSimpleName(), field
            );
            details.put("field", field);
            details.put("invalidValue", ex.getValue());
        }

        ErrorResponseDto dto = ErrorResponseDto.builder()
                .error("InvalidFormat")
                .status(HttpStatus.BAD_REQUEST.value())
                .message(message)
                .path(request.getRequestURI())
                .timestamp(OffsetDateTime.now())
                .details(details.isEmpty() ? null : details)
                .build();

        return ResponseEntity.badRequest().body(dto);
    }

    private String getFieldName(InvalidFormatException ex) {
        if (!ex.getPath().isEmpty()) {
            JsonMappingException.Reference ref = ex.getPath().get(ex.getPath().size() - 1);
            return ref.getFieldName() != null ? ref.getFieldName() : "unknown";
        }
        return "unknown";
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ErrorResponseDto> handlePropertyReferenceException(PropertyReferenceException e, HttpServletRequest request) {
        log.warn("Invalid sort parameter: {}", e.getMessage());
        ErrorResponseDto dto = ErrorResponseDto.builder()
                .error("BadRequest")
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Invalid sort parameter: " + e.getMessage())
                .path(request.getRequestURI())
                .timestamp(OffsetDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(dto);
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