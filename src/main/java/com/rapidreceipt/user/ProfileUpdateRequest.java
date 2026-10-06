package com.rapidreceipt.user;

import lombok.Data;

/**
 * Request body for PUT /api/profile.
 *
 * All fields are optional to allow partial profile updates (e.g., subscription tier only).
 */
@Data
public class ProfileUpdateRequest {

    private String businessName;
    private String businessAddress;
    private String phone;
    private String bankName;
    private String accountNumber;
    private String accountName;
    private String brandColor;
    private String subscriptionTier;
}
