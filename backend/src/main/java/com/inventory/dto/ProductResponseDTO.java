package com.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {

    private Long id;
    private String name;
    private String sku;
    private Long categoryId;
    private String categoryName;
    private String description;
    private BigDecimal purchasePrice;
    private BigDecimal sellingPrice;
    private int currentStock;
    private int minStockLevel;
    private int maxStockLevel;
    private String unit;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
