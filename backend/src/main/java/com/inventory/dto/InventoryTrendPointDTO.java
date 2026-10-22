package com.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One day's cumulative inventory value (Requirement 9.4).
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryTrendPointDTO {

    private LocalDate date;
    private BigDecimal inventoryValue;
}
