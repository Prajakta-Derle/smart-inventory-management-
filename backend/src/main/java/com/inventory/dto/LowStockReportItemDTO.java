package com.inventory.dto;

import com.inventory.entity.StockStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * One low/out-of-stock product with a recommended reorder quantity
 * (Requirement 10.5).
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LowStockReportItemDTO {

    private Long productId;
    private String productName;
    private String sku;
    private String categoryName;
    private int currentStock;
    private int minStockLevel;
    private int maxStockLevel;
    private StockStatus stockStatus;
    private int recommendedReorderQuantity;
}
