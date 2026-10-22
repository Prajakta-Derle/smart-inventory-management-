package com.inventory.service;

import com.inventory.dto.InventoryReportItemDTO;
import com.inventory.dto.LowStockReportItemDTO;
import com.inventory.dto.PurchaseReportDTO;
import com.inventory.dto.StockMovementReportDTO;
import com.inventory.dto.SupplierPurchaseReportItemDTO;
import com.inventory.entity.MovementType;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {

    PurchaseReportDTO getPurchaseReport(LocalDate start, LocalDate end);

    List<InventoryReportItemDTO> getInventoryReport(LocalDate start, LocalDate end);

    List<SupplierPurchaseReportItemDTO> getSupplierPurchaseReport(LocalDate start, LocalDate end);

    StockMovementReportDTO getStockMovementReport(LocalDate start, LocalDate end, MovementType movementType);

    List<LowStockReportItemDTO> getLowStockReport();
}
