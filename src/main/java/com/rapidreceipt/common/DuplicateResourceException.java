package com.rapidreceipt.common;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a create/register operation would violate a uniqueness constraint.
 * Maps to HTTP 409 Conflict.
 *
 * Usage examples:
 *   throw new DuplicateResourceException("Email already registered");
 */
public class DuplicateResourceException extends ApiException {

    public DuplicateResourceException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
