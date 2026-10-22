package com.inventory.dto;

import com.inventory.entity.Direction;
import com.inventory.entity.MovementType;
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
public class StockMovementResponseDTO {

    private Long id;
    private Long productId;
    private String productName;
    private String sku;
    private MovementType movementType;
    private Direction direction;
    private int quantity;
    private BigDecimal unitCost;
    private int stockBefore;
    private int stockAfter;
    private String note;
    private LocalDateTime timestamp;
    private String actingUsername;
}
