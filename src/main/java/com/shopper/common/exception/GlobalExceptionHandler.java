package com.shopper.common.exception;

import com.shopper.common.dto.ApiResponse;
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
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.mapping.PropertyReferenceException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("VALIDATION_ERROR", "Validation failed"));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<?> handleBind(BindException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("VALIDATION_ERROR", "Invalid request"));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("VALIDATION_ERROR", "Constraint violation"));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest request) {
        String message = "Missing required parameter: " + ex.getParameterName();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("VALIDATION_ERROR", message));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("UNAUTHORIZED", "Invalid email or password"));
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<?> handleLocked(LockedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.LOCKED).body(ApiResponse.error("ACCOUNT_LOCKED", ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("FORBIDDEN", "Access denied"));
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<?> handlePropertyReference(PropertyReferenceException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.error("VALIDATION_ERROR", "Invalid sort property: " + ex.getPropertyName()));
    }

    private static final Pattern UNKNOWN_PATH_PATTERN = Pattern.compile("Could not resolve attribute '([^']+)'");

    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ResponseEntity<?> handleInvalidDataAccessApiUsage(InvalidDataAccessApiUsageException ex,
            HttpServletRequest request) {
        String message = ex.getMessage();
        if (message != null && (message.contains("Could not resolve attribute") || message.contains("UnknownPathException"))) {
            Matcher m = UNKNOWN_PATH_PATTERN.matcher(message);
            String prop = m.find() ? m.group(1) : "unknown";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.error("VALIDATION_ERROR", "Invalid sort property: " + prop));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.error("VALIDATION_ERROR", message != null ? message : "Invalid request"));
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<?> handleBusinessRule(BusinessRuleException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
                ApiResponse.error("BUSINESS_RULE_VIOLATION", ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        String message = ex.getMessage();
        if (message != null && message.contains("idx_email_verification_otp_email_type")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    ApiResponse.error("BUSINESS_RULE_VIOLATION", "A verification code was already sent. Wait a moment before requesting another."));
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ApiResponse.error("CONFLICT", "A conflict occurred. Please try again."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("INTERNAL_ERROR", "An unexpected error occurred"));
    }
}
