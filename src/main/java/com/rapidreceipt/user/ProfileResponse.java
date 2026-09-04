package com.rapidreceipt.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response shape for GET /api/profile.
 *
 * Deliberately excludes the password field — a DTO acts as a firewall
 * between the internal entity and the API surface. Even if a developer
 * accidentally returns this object directly, no sensitive data leaks out.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {

    private Long id;
    private String email;
    private String businessName;
    private String phone;

    // Bank details
    private String bankName;
    private String accountNumber;
    private String accountName;

    // Branding
    private String logoUrl;
    private String brandColor;

    // Subscription
    private SubscriptionTier subscriptionTier;
    private SubscriptionStatus subscriptionStatus;

    private LocalDateTime createdAt;
}
