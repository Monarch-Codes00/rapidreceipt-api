package com.rapidreceipt.customer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Customer entities.
 *
 * All queries are scoped to a userId — a user must never be able to
 * read or modify another user's customers. The service layer enforces
 * this by always passing the authenticated user's ID into these queries.
 */
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /** Returns all customers belonging to the authenticated user, newest first. */
    List<Customer> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Finds a specific customer only if it belongs to the given user.
     * Returns empty if the customer exists but belongs to a different user —
     * the service treats this the same as "not found" (no information leakage).
     */
    Optional<Customer> findByIdAndUserId(Long id, Long userId);
}
