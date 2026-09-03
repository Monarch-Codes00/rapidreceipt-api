package com.rapidreceipt.invoice;

/**
 * Distinguishes the type of document being issued.
 * INVOICE — a bill sent to a customer before payment.
 * RECEIPT — a confirmation document issued after payment.
 */
public enum DocumentType {
    INVOICE,
    RECEIPT
}
