package com.rapidreceipt.invoice;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * A single line item within an Invoice.
 *
 * Key design decisions:
 * - We store serviceName as a plain String (not a FK to ServiceItem).
 *   This is intentional: if the user later edits or deletes a ServiceItem,
 *   the historical invoice must still show the original name and price.
 *   Snapshots of data at the time of invoicing are crucial for financial records.
 * - mappedBy = "invoice" tells Hibernate that the Invoice entity "owns" this
 *   relationship. The foreign key column (invoice_id) lives in this table.
 * - No createdAt needed here — the parent Invoice timestamp covers it.
 */
@Entity
@Table(name = "invoice_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "invoice")
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Back-reference to the parent invoice.
     * LAZY because we only need the parent when we explicitly request it.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    /**
     * Snapshot of the service name at the time the invoice was created.
     * NOT a foreign key — see class-level Javadoc for the reasoning.
     */
    @Column(nullable = false, length = 150)
    private String serviceName;

    @Column(nullable = false)
    private Integer quantity;

    /** Unit price at time of invoicing — snapshot, not live reference. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;
}
