package com.inventory.dto;

import com.inventory.entity.Direction;
import com.inventory.entity.MovementType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateMovementRequestDTO {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Movement type is required")
    private MovementType movementType;

    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 1_000_000, message = "Quantity must not exceed 1,000,000")
    private int quantity;

    /**
     * Required only when movementType is ADJUSTMENT; derived automatically for
     * PURCHASE / SALE / DAMAGED. Validated in the service layer (Req 7.9).
     */
    private Direction direction;

    @Size(max = 500, message = "Note must not exceed 500 characters")
    private String note;
}
