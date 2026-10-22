package com.inventory.controller;

import com.inventory.dto.ApiResponse;
import com.inventory.dto.InventoryReportItemDTO;
import com.inventory.dto.LowStockReportItemDTO;
import com.inventory.dto.PurchaseReportDTO;
import com.inventory.dto.StockMovementReportDTO;
import com.inventory.dto.SupplierPurchaseReportItemDTO;
import com.inventory.entity.MovementType;
import com.inventory.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * ADMIN, STAFF — purchase report over a date range.
     */
    @GetMapping("/purchase")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<PurchaseReportDTO>> getPurchaseReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        PurchaseReportDTO report = reportService.getPurchaseReport(startDate, endDate);
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK.value(), "Purchase report generated", report));
    }

    /**
     * ADMIN, STAFF — inventory report over a date range.
     */
    @GetMapping("/inventory")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<InventoryReportItemDTO>>> getInventoryReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<InventoryReportItemDTO> report = reportService.getInventoryReport(startDate, endDate);
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK.value(), "Inventory report generated", report));
    }

    /**
     * ADMIN, STAFF — supplier-purchase report over a date range.
     */
    @GetMapping("/supplier-purchase")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<SupplierPurchaseReportItemDTO>>> getSupplierPurchaseReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<SupplierPurchaseReportItemDTO> report =
                reportService.getSupplierPurchaseReport(startDate, endDate);
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK.value(), "Supplier purchase report generated", report));
    }

    /**
     * ADMIN, STAFF — stock-movement report over a date range with optional type filter.
     */
    @GetMapping("/stock-movements")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<StockMovementReportDTO>> getStockMovementReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) MovementType movementType) {

        StockMovementReportDTO report =
                reportService.getStockMovementReport(startDate, endDate, movementType);
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK.value(), "Stock movement report generated", report));
    }

    /**
     * ADMIN, STAFF — current low-stock report with recommended reorder quantities.
     */
    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<LowStockReportItemDTO>>> getLowStockReport() {

        List<LowStockReportItemDTO> report = reportService.getLowStockReport();
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK.value(), "Low-stock report generated", report));
    }
}
