package com.rapidreceipt.common;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a requested resource does not exist.
 * Maps to HTTP 404 Not Found.
 *
 * Usage examples:
 *   throw new ResourceNotFoundException("Customer not found");
 *   throw new ResourceNotFoundException("Invoice with ID " + id + " not found");
 */
public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
