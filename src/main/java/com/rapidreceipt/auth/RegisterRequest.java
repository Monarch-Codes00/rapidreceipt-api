package com.rapidreceipt.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body for POST /api/auth/register.
 *
 * @NotBlank — rejects null AND empty strings (unlike @NotNull).
 * @Email     — validates format like "user@example.com".
 * @Size      — enforces minimum password length before hashing.
 *
 * Validation is triggered by @Valid on the controller method parameter.
 * Errors are caught by GlobalExceptionHandler and returned as clean JSON.
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Business name is required")
    private String businessName;

    private String phone;
}
