package com.rapidreceipt.service;

import com.rapidreceipt.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a service or product that a User offers to their customers.
 * These items are the building blocks of an Invoice.
 *
 * Key design decisions:
 * - Price uses BigDecimal (not double/float) to avoid floating-point precision
 *   errors when dealing with money. This is critical for financial applications.
 * - precision=12, scale=2 supports amounts up to 9,999,999,999.99 (NGN friendly).
 * - description is TEXT (unlimited length) since service descriptions can be long.
 */
@Entity
@Table(name = "service_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "user")
public class ServiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 150)
    private String name;

    /** Optional longer description of the service. Stored as TEXT in PostgreSQL. */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Price in the smallest sensible unit for the business currency (NGN).
     * BigDecimal with (12, 2) precision is the correct type for monetary values.
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
