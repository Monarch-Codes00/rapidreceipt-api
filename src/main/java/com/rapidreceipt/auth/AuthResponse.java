package com.rapidreceipt.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response body returned after a successful register or login.
 *
 * Contains the JWT token the frontend must store and send in the
 * Authorization header on every subsequent request:
 *   Authorization: Bearer <token>
 *
 * We also return email and businessName so the frontend can
 * pre-populate the UI immediately without a second API call.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String email;
    private String businessName;
}
