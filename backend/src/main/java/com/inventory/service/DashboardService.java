package com.inventory.service;

import com.inventory.dto.DashboardSummaryDTO;
import com.inventory.dto.InventoryTrendPointDTO;
import com.inventory.dto.PurchaseTrendPointDTO;
import com.inventory.dto.StockMovementResponseDTO;

import java.time.LocalDate;
import java.util.List;

public interface DashboardService {

    DashboardSummaryDTO getSummary();

    List<StockMovementResponseDTO> getRecentMovements();

    List<PurchaseTrendPointDTO> getPurchaseTrend(LocalDate start, LocalDate end);

    List<InventoryTrendPointDTO> getInventoryTrend(LocalDate start, LocalDate end);
}
