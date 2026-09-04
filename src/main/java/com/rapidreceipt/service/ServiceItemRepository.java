package com.rapidreceipt.service;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ServiceItem entities.
 * Same user-scoping pattern as CustomerRepository — no cross-user data access.
 */
public interface ServiceItemRepository extends JpaRepository<ServiceItem, Long> {

    List<ServiceItem> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<ServiceItem> findByIdAndUserId(Long id, Long userId);
}
