package com.inventory.dto;

import com.inventory.validation.StockLevelProvider;
import com.inventory.validation.StockLevelValid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@StockLevelValid
public class CreateProductRequestDTO implements StockLevelProvider {

    @NotBlank(message = "Product name is required")
    @Size(max = 100, message = "Product name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "SKU is required")
    @Size(max = 50, message = "SKU must not exceed 50 characters")
    private String sku;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotNull(message = "Purchase price is required")
    @DecimalMin(value = "0.0", message = "Purchase price must not be negative")
    @Digits(integer = 10, fraction = 2, message = "Purchase price must have at most 2 decimal places")
    private BigDecimal purchasePrice;

    @NotNull(message = "Selling price is required")
    @DecimalMin(value = "0.0", message = "Selling price must not be negative")
    @Digits(integer = 10, fraction = 2, message = "Selling price must have at most 2 decimal places")
    private BigDecimal sellingPrice;

    @Min(value = 0, message = "Minimum stock level must not be negative")
    private int minStockLevel;

    @Min(value = 0, message = "Maximum stock level must not be negative")
    private int maxStockLevel;

    @NotBlank(message = "Unit of measure is required")
    @Size(max = 50, message = "Unit must not exceed 50 characters")
    private String unit;

    @Override
    public int getMinStockLevel() { return minStockLevel; }

    @Override
    public int getMaxStockLevel() { return maxStockLevel; }
}
