package com.rapidreceipt.invoice;

import org.springframework.data.jpa.repository.JpaRepository;

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
 */
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    /** All invoices for a user, most recent first. */
    List<Invoice> findByUserIdOrderByCreatedAtDesc(Long userId);

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
