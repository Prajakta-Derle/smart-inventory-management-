package com.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * One active product's inventory report row (Requirement 10.2).
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReportItemDTO {

    private Long productId;
    private String productName;
    private String sku;
    private int currentStock;
    private int minStockLevel;
    private int maxStockLevel;
    private long totalPurchaseQuantity;
    private long totalSaleQuantity;
    private long totalDamagedQuantity;
    private long totalAdjustmentQuantity;
}
