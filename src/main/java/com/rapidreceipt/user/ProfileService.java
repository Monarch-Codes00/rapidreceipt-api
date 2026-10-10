package com.rapidreceipt.user;

import com.rapidreceipt.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service for managing the authenticated user's profile.
 */
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;

    public ProfileResponse getProfile(User user) {
        // Fetch fresh from DB to guarantee we don't return stale token data
        User freshUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));
        return mapToResponse(freshUser);
    }

    public ProfileResponse updateProfile(ProfileUpdateRequest request, User user) {
        User freshUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));

        if (request.getBusinessName() != null) freshUser.setBusinessName(request.getBusinessName());
        if (request.getBusinessAddress() != null) freshUser.setBusinessAddress(request.getBusinessAddress());
        if (request.getPhone() != null) freshUser.setPhone(request.getPhone());
        if (request.getBankName() != null) freshUser.setBankName(request.getBankName());
        if (request.getAccountNumber() != null) freshUser.setAccountNumber(request.getAccountNumber());
        if (request.getAccountName() != null) freshUser.setAccountName(request.getAccountName());
        if (request.getBrandColor() != null) freshUser.setBrandColor(request.getBrandColor());
        if (request.getSubscriptionTier() != null && !request.getSubscriptionTier().isBlank()) {
            try {
                freshUser.setSubscriptionTier(SubscriptionTier.valueOf(request.getSubscriptionTier().toUpperCase()));
                freshUser.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
            } catch (Exception ignored) {
            }
        }

        User updatedUser = userRepository.save(freshUser);
        return mapToResponse(updatedUser);
    }

    private ProfileResponse mapToResponse(User user) {
        return ProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .businessName(user.getBusinessName())
                .businessAddress(user.getBusinessAddress())
                .phone(user.getPhone())
                .bankName(user.getBankName())
                .accountNumber(user.getAccountNumber())
                .accountName(user.getAccountName())
                .logoUrl(sanitizeLogoUrl(user.getLogoUrl()))
                .brandColor(user.getBrandColor())
                .subscriptionTier(user.getSubscriptionTier())
                .subscriptionStatus(user.getSubscriptionStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private String sanitizeLogoUrl(String logoUrl) {
        if (logoUrl == null || logoUrl.isBlank()) {
            return null;
        }
        if (logoUrl.startsWith("http://") || logoUrl.startsWith("https://") || logoUrl.startsWith("data:image")) {
            return logoUrl;
        }
        return null;
    }
}
