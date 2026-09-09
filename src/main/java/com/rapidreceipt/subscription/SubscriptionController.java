package com.rapidreceipt.subscription;

import com.rapidreceipt.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/initialize")
    public ResponseEntity<PaystackInitializeResponse.PaystackData> initializePayment(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, String> payload) {
        
        // Amount is passed from the frontend in kobo (e.g. "500000" for NGN 5000.00)
        String amountInKobo = payload.getOrDefault("amount", "500000"); 

        return ResponseEntity.ok(subscriptionService.initializePayment(user, amountInKobo));
    }

    @GetMapping("/verify")
    public ResponseEntity<Map<String, String>> verifyPayment(
            @RequestParam String reference,
            @AuthenticationPrincipal User user) {
        
        subscriptionService.verifyPayment(reference, user);
        return ResponseEntity.ok(Map.of(
                "message", "Subscription upgraded successfully",
                "status", "success"
        ));
    }
}
