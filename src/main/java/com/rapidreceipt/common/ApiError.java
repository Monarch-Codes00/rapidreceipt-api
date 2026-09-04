package com.rapidreceipt.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * The standard error response shape returned by every API error.
 *
 * Every error from this API — validation failures, not found, conflicts,
 * and unexpected server errors — comes back in this exact shape:
 *
 * {
 *   "status": 404,
 *   "message": "Customer not found",
 *   "timestamp": "2024-01-15T10:30:00"
 * }
 *
 * This consistency makes the frontend's error handling code simple:
 * always look at response.data.message.
 */
@Data
@Builder
@AllArgsConstructor
public class ApiError {

    private int status;
    private String message;
    private LocalDateTime timestamp;
}
