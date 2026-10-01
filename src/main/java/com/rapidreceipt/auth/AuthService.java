package com.rapidreceipt.auth;

import com.rapidreceipt.common.DuplicateResourceException;
import com.rapidreceipt.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.rapidreceipt.common.ResourceNotFoundException;
import com.rapidreceipt.common.ApiException;
import com.rapidreceipt.common.EmailService;
import org.springframework.http.HttpStatus;
import java.time.LocalDateTime;

/**
 * Handles user registration and login business logic.
 *
 * Register flow:
 *   1. Check for duplicate email — throw 409 if already taken.
 *   2. Hash the password with BCrypt.
 *   3. Save the new User to the DB.
 *   4. Generate a JWT for immediate login after registration.
 *   5. Return the token + basic user info.
 *
 * Login flow:
 *   1. Call AuthenticationManager.authenticate() — this internally:
 *      a. Loads the User via UserDetailsService (by email).
 *      b. Compares the raw password against the BCrypt hash.
 *      c. Throws BadCredentialsException if they don't match (Spring handles the 401).
 *   2. Load the User again to get the full entity for token generation.
 *   3. Generate and return the JWT.
 *
 * Note: AuthService never manually checks the password — it delegates that
 * entirely to AuthenticationManager, which uses the BCryptPasswordEncoder
 * configured in SecurityConfig.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;

    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .businessName(request.getBusinessName())
                .phone(request.getPhone())
                .subscriptionTier(SubscriptionTier.FREE)
                .subscriptionStatus(SubscriptionStatus.TRIAL)
                .build();

        userRepository.save(user);

        String otp = String.format("%06d", new java.util.Random().nextInt(999999));
        user.setRegistrationOtp(otp);
        user.setRegistrationOtpExpiry(LocalDateTime.now().plusMinutes(15));
        user.setVerified(false);

        userRepository.save(user);

        emailService.sendOtpEmail(user.getEmail(), otp, "registration");

        return RegisterResponse.builder()
                .message("OTP sent to email successfully")
                .email(user.getEmail())
                .build();
    }

    public AuthResponse verifyRegistration(VerifyRegistrationRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.isVerified()) {
            throw new ApiException("User is already verified", HttpStatus.BAD_REQUEST);
        }

        if (user.getRegistrationOtp() == null || !user.getRegistrationOtp().equals(request.getOtp())) {
            throw new ApiException("Invalid OTP", HttpStatus.BAD_REQUEST);
        }

        if (user.getRegistrationOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new ApiException("OTP has expired", HttpStatus.BAD_REQUEST);
        }

        user.setVerified(true);
        user.setRegistrationOtp(null);
        user.setRegistrationOtpExpiry(null);
        userRepository.save(user);

        String token = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken.getToken())
                .email(user.getEmail())
                .businessName(user.getBusinessName())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        // Throws BadCredentialsException automatically if credentials are wrong
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));

        String token = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken.getToken())
                .email(user.getEmail())
                .businessName(user.getBusinessName())
                .build();
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User with this email does not exist"));

        // Generate 6 digit OTP
        String otp = String.format("%06d", new java.util.Random().nextInt(999999));
        
        user.setResetOtp(otp);
        user.setResetOtpExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        emailService.sendOtpEmail(user.getEmail(), otp, "password_reset");
    }

    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User with this email does not exist"));

        if (user.getResetOtp() == null || !user.getResetOtp().equals(request.getOtp())) {
            throw new ApiException("Invalid OTP", HttpStatus.BAD_REQUEST);
        }

        if (user.getResetOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new ApiException("OTP has expired", HttpStatus.BAD_REQUEST);
        }

        // Reset the password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        
        // Clear the OTP
        user.setResetOtp(null);
        user.setResetOtpExpiry(null);
        
        userRepository.save(user);
    }

    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
        return refreshTokenService.findByToken(request.getRefreshToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtService.generateToken(user);
                    return TokenRefreshResponse.builder()
                            .token(token)
                            .refreshToken(request.getRefreshToken())
                            .build();
                })
                .orElseThrow(() -> new ApiException("Refresh token is not in database!", HttpStatus.UNAUTHORIZED));
    }
}
