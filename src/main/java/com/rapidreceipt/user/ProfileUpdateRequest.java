package com.rapidreceipt.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for PUT /api/profile.
 *
 * Only mutable fields are included — email changes are not allowed here
 * (they require a separate verification flow). Password changes will have
 * their own dedicated endpoint in a later iteration.
 */
@Data
public class ProfileUpdateRequest {

    @NotBlank(message = "Business name is required")
    private String businessName;

    private String phone;
    private String bankName;
    private String accountNumber;
    private String accountName;
    private String brandColor;
}
