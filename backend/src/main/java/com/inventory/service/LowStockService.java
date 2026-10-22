package com.inventory.service;

import com.inventory.dto.LowStockItemDTO;
import com.inventory.dto.LowStockSummaryDTO;
import com.inventory.dto.PageResponseDTO;
import com.inventory.entity.StockStatus;
import org.springframework.data.domain.Pageable;

public interface LowStockService {

    /**
     * Active products at or below minimum stock level. An optional
     * stock-status filter narrows the result to LOW_STOCK or OUT_OF_STOCK.
     */
    PageResponseDTO<LowStockItemDTO> getLowStockProducts(StockStatus filter, Pageable pageable);

    PageResponseDTO<LowStockItemDTO> getOutOfStockProducts(Pageable pageable);

    LowStockSummaryDTO getLowStockSummary();
}
