package com.rapidreceipt.service;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Repository for ServiceItem entities.
 * Same user-scoping pattern as CustomerRepository — no cross-user data access.
 */
public interface ServiceItemRepository extends JpaRepository<ServiceItem, Long> {

    Page<ServiceItem> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<ServiceItem> findByIdAndUserId(Long id, Long userId);
}
