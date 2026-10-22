package com.inventory.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class PurchaseOrderItemRequestDTO {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @Min(value = 1, message = "Quantity must be greater than zero")
    @Max(value = 1_000_000, message = "Quantity must not exceed 1,000,000")
    private int quantity;

    @NotNull(message = "Purchase price is required")
    @DecimalMin(value = "0.01", message = "Purchase price must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Purchase price must have at most 2 decimal places")
    private BigDecimal purchasePrice;
}
