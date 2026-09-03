package com.rapidreceipt.user;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a registered business owner on the RapidReceipt platform.
 *
 * Key design decisions:
 * - Table is named "users" because "user" is a reserved word in PostgreSQL.
 * - We use @Getter/@Setter instead of @Data to avoid Lombok auto-generating
 *   equals/hashCode based on lazy-loaded JPA relationships (a common footgun).
 * - Password is excluded from toString() for security.
 * - Subscription fields default to FREE/TRIAL so new users need no extra setup.
 * - @CreationTimestamp is a Hibernate annotation — it stamps the field on INSERT
 *   without needing @EnableJpaAuditing on the application class.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "password")
public class User {

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

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
