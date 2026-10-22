package com.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderItemResponseDTO {

    private Long id;
    private Long productId;
    private String productName;
    private String sku;
    private int quantity;
    private BigDecimal purchasePrice;
    private BigDecimal subtotal;
}
