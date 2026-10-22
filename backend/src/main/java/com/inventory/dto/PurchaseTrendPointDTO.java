package com.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One day's aggregated purchase-order total (Requirement 9.3).
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseTrendPointDTO {

    private LocalDate date;
    private BigDecimal totalAmount;
}
