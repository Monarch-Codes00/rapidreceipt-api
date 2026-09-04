package com.rapidreceipt.invoice;

import com.rapidreceipt.customer.CustomerResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Full response shape for invoice endpoints.
 *
 * Embeds a CustomerResponse (not just customerId) so the frontend
 * can render the customer's name and contact details without a second API call.
 * This is the "N+1 avoidance at the API level" pattern — compose the response
 * server-side rather than forcing the frontend to make multiple requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {

    private Long id;
    private String invoiceNumber;
    private DocumentType documentType;

    /** Embedded customer info — avoids an extra fetch by the frontend. */
    private CustomerResponse customer;

    private LocalDate issueDate;
    private LocalDate dueDate;

    private List<InvoiceItemResponse> items;

    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal total;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
