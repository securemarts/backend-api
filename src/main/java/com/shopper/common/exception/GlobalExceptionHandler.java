package com.shopper.common.exception;

import com.shopper.common.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.mapping.PropertyReferenceException;

import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiError.builder()
                        .timestamp(Instant.now())
                        .status(HttpStatus.NOT_FOUND.value())
                        .error("NOT_FOUND")
                        .message(ex.getMessage())
                        .path(request.getRequestURI())
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ApiError.FieldError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ApiError.FieldError(fe.getField(), fe.getDefaultMessage(), fe.getRejectedValue()))
                .collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiError.builder()
                        .timestamp(Instant.now())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error("VALIDATION_ERROR")
                        .message("Validation failed")
                        .path(request.getRequestURI())
                        .fieldErrors(errors)
                        .build());
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiError> handleBind(BindException ex, HttpServletRequest request) {
        List<ApiError.FieldError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ApiError.FieldError(fe.getField(), fe.getDefaultMessage(), fe.getRejectedValue()))
                .collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiError.builder()
                        .timestamp(Instant.now())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error("VALIDATION_ERROR")
                        .message("Invalid request")
                        .path(request.getRequestURI())
                        .fieldErrors(errors)
                        .build());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        List<ApiError.FieldError> errors = ex.getConstraintViolations().stream()
                .map(v -> new ApiError.FieldError(
                        v.getPropertyPath().toString(),
                        v.getMessage(),
                        v.getInvalidValue()))
                .collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiError.builder()
                        .timestamp(Instant.now())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error("VALIDATION_ERROR")
                        .message("Constraint violation")
                        .path(request.getRequestURI())
                        .fieldErrors(errors)
                        .build());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiError.builder()
                        .timestamp(Instant.now())
                        .status(HttpStatus.UNAUTHORIZED.value())
                        .error("UNAUTHORIZED")
                        .message("Invalid email or password")
                        .path(request.getRequestURI())
                        .build());
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiError> handleLocked(LockedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.LOCKED).body(
                ApiError.builder()
                        .timestamp(Instant.now())
                        .status(HttpStatus.LOCKED.value())
                        .error("ACCOUNT_LOCKED")
                        .message(ex.getMessage())
                        .path(request.getRequestURI())
                        .build());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ApiError.builder()
                        .timestamp(Instant.now())
                        .status(HttpStatus.FORBIDDEN.value())
                        .error("FORBIDDEN")
                        .message("Access denied")
                        .path(request.getRequestURI())
                        .build());
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ApiError> handlePropertyReference(PropertyReferenceException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiError.builder()
                        .timestamp(Instant.now())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error("VALIDATION_ERROR")
                        .message("Invalid sort property: " + ex.getPropertyName())
                        .path(request.getRequestURI())
                        .build());
    }

    private static final Pattern UNKNOWN_PATH_PATTERN = Pattern.compile("Could not resolve attribute '([^']+)'");

    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ResponseEntity<ApiError> handleInvalidDataAccessApiUsage(InvalidDataAccessApiUsageException ex,
            HttpServletRequest request) {
        String message = ex.getMessage();
        if (message != null && (message.contains("Could not resolve attribute") || message.contains("UnknownPathException"))) {
            Matcher m = UNKNOWN_PATH_PATTERN.matcher(message);
            String prop = m.find() ? m.group(1) : "unknown";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiError.builder()
                            .timestamp(Instant.now())
                            .status(HttpStatus.BAD_REQUEST.value())
                            .error("VALIDATION_ERROR")
                            .message("Invalid sort property: " + prop)
                            .path(request.getRequestURI())
                            .build());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiError.builder()
                        .timestamp(Instant.now())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error("VALIDATION_ERROR")
                        .message(message != null ? message : "Invalid request")
                        .path(request.getRequestURI())
                        .build());
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiError> handleBusinessRule(BusinessRuleException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
                ApiError.builder()
                        .timestamp(Instant.now())
                        .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .error("BUSINESS_RULE_VIOLATION")
                        .message(ex.getMessage())
                        .path(request.getRequestURI())
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiError.builder()
                        .timestamp(Instant.now())
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .error("INTERNAL_ERROR")
                        .message("An unexpected error occurred")
                        .path(request.getRequestURI())
                        .build());
    }
}
