package com.inventory.controller;

import com.inventory.dto.ApiResponse;
import com.inventory.dto.LowStockItemDTO;
import com.inventory.dto.LowStockSummaryDTO;
import com.inventory.dto.PageResponseDTO;
import com.inventory.entity.StockStatus;
import com.inventory.service.LowStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/low-stock")
@RequiredArgsConstructor
public class LowStockController {

    private final LowStockService lowStockService;

    /**
     * ADMIN, STAFF — paginated low/out-of-stock product list with an optional
     * stock-status filter (LOW_STOCK or OUT_OF_STOCK).
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<PageResponseDTO<LowStockItemDTO>>> getLowStock(
            @RequestParam(name = "stock-status", required = false) StockStatus stockStatus,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("currentStock").ascending());
        PageResponseDTO<LowStockItemDTO> result =
                lowStockService.getLowStockProducts(stockStatus, pageable);
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK.value(), "Low-stock products retrieved", result));
    }

    /**
     * ADMIN, STAFF — paginated out-of-stock product list.
     */
    @GetMapping("/out-of-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<PageResponseDTO<LowStockItemDTO>>> getOutOfStock(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        PageResponseDTO<LowStockItemDTO> result =
                lowStockService.getOutOfStockProducts(pageable);
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK.value(), "Out-of-stock products retrieved", result));
    }

    /**
     * ADMIN, STAFF — low-stock summary with aggregate counts and the
     * top-10 most critical products.
     */
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<LowStockSummaryDTO>> getSummary() {

        LowStockSummaryDTO summary = lowStockService.getLowStockSummary();
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK.value(), "Low-stock summary retrieved", summary));
    }
}
