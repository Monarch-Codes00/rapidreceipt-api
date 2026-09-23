package com.rapidreceipt.user;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Represents a registered business owner on the RapidReceipt platform.
 *
 * Implements UserDetails so Spring Security can use this entity directly
 * as its authentication principal — no wrapper class needed.
 *
 * Key design decisions:
 * - Table is named "users" because "user" is a reserved word in PostgreSQL.
 * - We use @Getter/@Setter instead of @Data to avoid Lombok auto-generating
 *   equals/hashCode based on lazy-loaded JPA relationships (a common footgun).
 * - Password is excluded from toString() for security.
 * - Lombok's @Getter on the `password` field satisfies UserDetails.getPassword().
 * - getUsername() returns email — our login identifier, not a separate field.
 * - getAuthorities() returns an empty list — roles/permissions come later.
 * - All account status booleans return true — no suspension logic yet.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "password")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Must be unique — used as the login identifier. */
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /** Stored as a BCrypt hash — never plain text. */
    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 150)
    private String businessName;

    @Column(length = 20)
    private String phone;

    // --- Bank / payout details ---
    @Column(length = 100)
    private String bankName;

    @Column(length = 20)
    private String accountNumber;

    @Column(length = 150)
    private String accountName;

    // --- Branding ---
    private String logoUrl;

    /** Stored as a hex colour string e.g. "#FF5733". */
    @Column(length = 10)
    private String brandColor;

    // --- Subscription ---
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SubscriptionTier subscriptionTier = SubscriptionTier.FREE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SubscriptionStatus subscriptionStatus = SubscriptionStatus.TRIAL;

    // --- Password Reset ---
    @Column(length = 6)
    private String resetOtp;

    private LocalDateTime resetOtpExpiry;

    // --- Verification ---
    @Builder.Default
    private boolean isVerified = false;

    @Column(length = 6)
    private String registrationOtp;

    private LocalDateTime registrationOtpExpiry;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // -------------------------------------------------------------------------
    // UserDetails implementation
    // -------------------------------------------------------------------------

    /**
     * Spring Security calls this to get the login identifier.
     * We use email as the username — there is no separate username field.
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Returns the roles/permissions for this user.
     * Empty for now — role-based access control is a future iteration.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    // All account status checks return true — no locking/expiry logic yet.
    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return isVerified; }
}

