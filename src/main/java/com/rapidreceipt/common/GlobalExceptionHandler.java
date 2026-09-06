package com.rapidreceipt.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Central error handler for the entire application.
 *
 * @RestControllerAdvice intercepts exceptions thrown from any @RestController
 * and converts them into clean JSON error responses using ApiError.
 *
 * Three handlers cover everything:
 * 1. ApiException (and subclasses) — known, intentional domain errors.
 * 2. MethodArgumentNotValidException — Bean Validation (@Valid) failures.
 * 3. Exception — catch-all for unexpected runtime errors.
 *
 * This means controllers and services never need try-catch blocks —
 * they simply throw and this class handles the HTTP response.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles all known domain errors (ResourceNotFoundException,
     * DuplicateResourceException, and any custom ApiException subclass).
     * The HTTP status is read directly from the exception.
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApiException(ApiException ex) {
        ApiError error = ApiError.builder()
                .status(ex.getStatus().value())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    /**
     * Handles Bean Validation failures from @Valid annotated request bodies.
     * Collects all field error messages into a single readable string.
     *
     * Example output: "Email is required, Password must be at least 8 characters"
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        ApiError error = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Handles bad credentials during login.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentialsException(BadCredentialsException ex) {
        ApiError error = ApiError.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .message("Invalid email or password")
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Catch-all for unexpected errors.
     * Returns a generic 500 message — never expose raw exception details to clients.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex) {
        ApiError error = ApiError.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("An unexpected error occurred. Please try again.")
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.internalServerError().body(error);
    }
}
