package com.rapidreceipt.customer;

import com.rapidreceipt.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a client/customer that belongs to a specific User (business owner).
 *
 * Key design decisions:
 * - @ManyToOne with FetchType.LAZY — we don't want to auto-load the entire
 *   User object every time we fetch a Customer. We'll load it explicitly when needed.
 * - @JoinColumn(name = "user_id") creates the foreign key column in the DB.
 * - The relationship is "many customers → one user" (a business has many customers).
 */
@Entity
@Table(name = "customers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "user")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The business owner this customer belongs to.
     * LAZY fetch means Hibernate does NOT automatically JOIN to the users table.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(length = 255)
    private String address;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
