package com.rapidreceipt.subscription;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaystackInitializeRequest {
    private String email;
    private String amount; // Amount in kobo
    private String callback_url; // Optional: Paystack redirects here after payment
}
