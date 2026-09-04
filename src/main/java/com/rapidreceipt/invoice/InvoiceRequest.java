package com.rapidreceipt.invoice;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Request body for POST /api/invoices.
 *
 * Key design decisions:
 * - @Valid on the items list cascades Bean Validation into each
 *   InvoiceItemRequest, so item-level errors are caught automatically.
 * - @NotEmpty on items ensures an invoice must have at least one line item.
 * - discount defaults to ZERO — optional field, not required from the frontend.
 * - documentType defaults to INVOICE — frontend only needs to send "RECEIPT"
 *   when explicitly issuing a receipt.
 * - subtotal and total are NOT in this request — InvoiceService calculates
 *   them from the items list to prevent client-side tampering.
 */
@Data
public class InvoiceRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Issue date is required")
    private LocalDate issueDate;

    /** Optional for receipts — no due date needed when payment is already done. */
    private LocalDate dueDate;

    private DocumentType documentType = DocumentType.INVOICE;

    @NotEmpty(message = "Invoice must have at least one item")
    @Valid
    private List<InvoiceItemRequest> items;

    private BigDecimal discount = BigDecimal.ZERO;
}
