package com.inventory.dto;

import com.inventory.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderResponseDTO {

    private Long id;
    private String orderNumber;
    private Long supplierId;
    private String supplierName;
    private LocalDate orderDate;
    private LocalDate expectedDeliveryDate;
    private OrderStatus status;
    private BigDecimal total;
    private List<PurchaseOrderItemResponseDTO> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
