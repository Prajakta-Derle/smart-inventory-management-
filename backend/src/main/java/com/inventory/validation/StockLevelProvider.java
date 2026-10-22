package com.inventory.validation;

/**
 * Implemented by product request DTOs so the {@link StockLevelValidator}
 * can read both stock-level bounds for the cross-field check.
 */
public interface StockLevelProvider {

    int getMinStockLevel();

    int getMaxStockLevel();
}
