package com.rapidreceipt.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response shape for service item endpoints.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceItemResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private LocalDateTime createdAt;
}
