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

        freshUser.setBusinessName(request.getBusinessName());
        freshUser.setPhone(request.getPhone());
        freshUser.setBankName(request.getBankName());
        freshUser.setAccountNumber(request.getAccountNumber());
        freshUser.setAccountName(request.getAccountName());
        freshUser.setBrandColor(request.getBrandColor());

        User updatedUser = userRepository.save(freshUser);
        return mapToResponse(updatedUser);
    }

    private ProfileResponse mapToResponse(User user) {
        return ProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .businessName(user.getBusinessName())
                .phone(user.getPhone())
                .bankName(user.getBankName())
                .accountNumber(user.getAccountNumber())
                .accountName(user.getAccountName())
                .logoUrl(user.getLogoUrl())
                .brandColor(user.getBrandColor())
                .subscriptionTier(user.getSubscriptionTier())
                .subscriptionStatus(user.getSubscriptionStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
