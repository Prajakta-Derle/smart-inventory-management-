package com.inventory.entity;

/**
 * Classification of a product's stock level for low-stock reporting
 * (Requirements 8.1, 8.2).
 */
public enum StockStatus {
    /** currentStock &gt; 0 and currentStock &le; minStockLevel. */
    LOW_STOCK,
    /** currentStock == 0. */
    OUT_OF_STOCK
}
