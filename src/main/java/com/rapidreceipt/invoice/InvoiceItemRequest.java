package com.rapidreceipt.invoice;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * A single line item inside an InvoiceRequest.
 *
 * Note: @Valid on the items list in InvoiceRequest cascades validation
 * into each InvoiceItemRequest automatically.
 */
@Data
public class InvoiceItemRequest {

    @NotBlank(message = "Service name is required")
    private String serviceName;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Item price must be greater than 0")
    private BigDecimal price;
}
