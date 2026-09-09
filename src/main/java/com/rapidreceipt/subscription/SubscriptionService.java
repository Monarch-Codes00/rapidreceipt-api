package com.rapidreceipt.subscription;

import com.rapidreceipt.common.ApiException;
import com.rapidreceipt.user.User;
import com.rapidreceipt.user.UserRepository;
import com.rapidreceipt.user.SubscriptionTier;
import com.rapidreceipt.user.SubscriptionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    @Value("${paystack.secret-key}")
    private String paystackSecretKey;

    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public PaystackInitializeResponse.PaystackData initializePayment(User user, String amountInKobo) {
        String url = "https://api.paystack.co/transaction/initialize";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + paystackSecretKey);
        headers.set("Content-Type", "application/json");

        PaystackInitializeRequest request = PaystackInitializeRequest.builder()
                .email(user.getEmail())
                .amount(amountInKobo)
                .build();

        HttpEntity<PaystackInitializeRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<PaystackInitializeResponse> response = restTemplate.postForEntity(url, entity, PaystackInitializeResponse.class);
            if (response.getBody() != null && response.getBody().isStatus()) {
                return response.getBody().getData();
            } else {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Failed to initialize payment with Paystack");
            }
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Error communicating with Paystack: " + e.getMessage());
        }
    }

    public void verifyPayment(String reference, User user) {
        String url = "https://api.paystack.co/transaction/verify/" + reference;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + paystackSecretKey);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<PaystackVerifyResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, PaystackVerifyResponse.class);
            PaystackVerifyResponse body = response.getBody();

            if (body != null && body.isStatus() && "success".equals(body.getData().getStatus())) {
                // Payment was successful! Upgrade the user's subscription
                user.setSubscriptionTier(SubscriptionTier.PRO);
                user.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
                userRepository.save(user);
            } else {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Payment verification failed or is not successful");
            }
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Error verifying payment with Paystack: " + e.getMessage());
        }
    }
}
