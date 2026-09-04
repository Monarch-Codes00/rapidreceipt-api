package com.rapidreceipt.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base exception for all known, intentional application errors.
 *
 * Services throw subclasses of this (ResourceNotFoundException,
 * DuplicateResourceException, etc.). GlobalExceptionHandler catches
 * ApiException and maps it to a proper HTTP response automatically.
 *
 * By carrying the HttpStatus inside the exception, the service layer
 * can express intent ("this is a 404") without importing Spring MVC
 * classes into every service — the handler does the translation.
 */
@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
