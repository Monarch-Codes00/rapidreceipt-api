package com.rapidreceipt.subscription;

import lombok.Data;

@Data
public class PaystackVerifyResponse {
    private boolean status;
    private String message;
    private VerifyData data;

    @Data
    public static class VerifyData {
        private String status; // "success", "failed", etc.
        private String reference;
        private int amount;
        private String gateway_response;
    }
}
