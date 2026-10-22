package com.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Aggregate low-stock summary with the top-N most critical products
 * (Requirement 8.5).
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LowStockSummaryDTO {

    private long lowStockCount;
    private long outOfStockCount;
    private List<LowStockItemDTO> topCritical;
}
