package com.inventory.dto;

import com.inventory.entity.StockStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * A single low/out-of-stock product entry (Requirement 8.8).
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LowStockItemDTO {

    private Long productId;
    private String productName;
    private String sku;
    private String categoryName;
    private int currentStock;
    private int minStockLevel;
    private StockStatus stockStatus;
}
