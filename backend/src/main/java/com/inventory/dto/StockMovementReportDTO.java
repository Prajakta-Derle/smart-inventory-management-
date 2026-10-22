package com.inventory.dto;

import com.inventory.entity.MovementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Stock movement report over a date range with an optional type filter
 * (Requirement 10.4).
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementReportDTO {

    private List<StockMovementResponseDTO> movements;
    private Map<MovementType, Long> totalQuantityByType;
}
