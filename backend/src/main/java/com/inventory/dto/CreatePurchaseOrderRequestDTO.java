package com.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CreatePurchaseOrderRequestDTO {

    @NotNull(message = "Supplier ID is required")
    private Long supplierId;

    @NotNull(message = "Order date is required")
    private LocalDate orderDate;

    @NotNull(message = "Expected delivery date is required")
    private LocalDate expectedDeliveryDate;

    @NotEmpty(message = "At least one order item is required")
    @Valid
    private List<PurchaseOrderItemRequestDTO> items;
}
