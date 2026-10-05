package com.rapidreceipt.invoice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Invoice entities.
 *
 * Extra methods beyond the standard CRUD:
 * - countByUserId: used by InvoiceService to generate sequential invoice numbers
 *   per user (e.g. user's 42nd invoice → "RR-2024-00042").
 * - existsByInvoiceNumber: safety check to guarantee invoice number uniqueness
 *   before persisting.
 *
 * NOTE: findByUserIdOrderByCreatedAtDesc uses JOIN FETCH to eagerly load
 * customer and items in a single query, preventing LazyInitializationException
 * when open-in-view is disabled.
 */
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    /**
     * All invoices for a user, most recent first, with customer and items
     * eagerly fetched via JOIN FETCH to avoid LazyInitializationException.
     */
    @Query("SELECT DISTINCT i FROM Invoice i " +
           "JOIN FETCH i.customer " +
           "LEFT JOIN FETCH i.items " +
           "WHERE i.user.id = :userId " +
           "ORDER BY i.createdAt DESC")
    List<Invoice> findByUserIdWithDetails(@Param("userId") Long userId);

    /** Fetch a specific invoice with customer and items eagerly loaded. */
    @Query("SELECT i FROM Invoice i " +
           "JOIN FETCH i.customer " +
           "LEFT JOIN FETCH i.items " +
           "WHERE i.id = :id AND i.user.id = :userId")
    Optional<Invoice> findByIdAndUserIdWithDetails(@Param("id") Long id, @Param("userId") Long userId);

    /** Fetch a specific invoice, guarding against cross-user access. */
    Optional<Invoice> findByIdAndUserId(Long id, Long userId);

    /** Uniqueness guard — invoice number must never collide. */
    boolean existsByInvoiceNumber(String invoiceNumber);

    /**
     * Count of invoices for a user — used to generate sequential invoice numbers.
     * e.g. if user has 41 invoices, next number is padded to "00042".
     */
    long countByUserId(Long userId);
}
