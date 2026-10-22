package com.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Dashboard KPI metrics (Requirement 9.1).
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDTO {

    private long totalActiveProducts;
    private long totalActiveSuppliers;
    private BigDecimal totalInventoryValue;
    private long totalPurchaseOrders;
    private long orderedPurchaseOrders;
    private long lowStockCount;
    private long outOfStockCount;
}
