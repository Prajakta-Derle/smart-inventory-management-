package com.inventory.controller;

import com.inventory.dto.ApiResponse;
import com.inventory.dto.DashboardSummaryDTO;
import com.inventory.dto.InventoryTrendPointDTO;
import com.inventory.dto.PurchaseTrendPointDTO;
import com.inventory.dto.StockMovementResponseDTO;
import com.inventory.service.DashboardService;
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
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * ADMIN, STAFF — aggregate KPI metrics.
     */
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<DashboardSummaryDTO>> getSummary() {

        DashboardSummaryDTO summary = dashboardService.getSummary();
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK.value(), "Dashboard summary retrieved", summary));
    }

    /**
     * ADMIN, STAFF — the 10 most recent stock movements.
     */
    @GetMapping("/recent-movements")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<StockMovementResponseDTO>>> getRecentMovements() {

        List<StockMovementResponseDTO> movements = dashboardService.getRecentMovements();
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK.value(), "Recent movements retrieved", movements));
    }

    /**
     * ADMIN, STAFF — daily purchase-order totals within a date range
     * (ORDERED or RECEIVED orders only).
     */
    @GetMapping("/purchase-trend")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<PurchaseTrendPointDTO>>> getPurchaseTrend(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<PurchaseTrendPointDTO> trend = dashboardService.getPurchaseTrend(startDate, endDate);
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK.value(), "Purchase trend retrieved", trend));
    }

    /**
     * ADMIN, STAFF — cumulative daily inventory value within a date range.
     */
    @GetMapping("/inventory-trend")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<InventoryTrendPointDTO>>> getInventoryTrend(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<InventoryTrendPointDTO> trend = dashboardService.getInventoryTrend(startDate, endDate);
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK.value(), "Inventory trend retrieved", trend));
    }
}
