package com.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Lightweight product projection used when listing products by category
 * (Requirement 4.5). The full product response shape is owned by the
 * Product module (task 8); this keeps the category feature self-contained.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryProductDTO {

    private Long id;
    private String name;
    private String sku;
    private BigDecimal purchasePrice;
    private BigDecimal sellingPrice;
    private int currentStock;
    private String unit;
    private boolean active;
}
