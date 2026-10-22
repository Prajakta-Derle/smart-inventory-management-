package com.inventory.controller;

import com.inventory.dto.ApiResponse;
import com.inventory.dto.CreateMovementRequestDTO;
import com.inventory.dto.PageResponseDTO;
import com.inventory.dto.StockMovementResponseDTO;
import com.inventory.entity.MovementType;
import com.inventory.service.StockMovementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/stock-movements")
@RequiredArgsConstructor
public class StockMovementController {

    private final StockMovementService stockMovementService;

    /**
     * ADMIN, STAFF — record a stock movement (immutable). Returns HTTP 201.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<StockMovementResponseDTO>> recordMovement(
            @Valid @RequestBody CreateMovementRequestDTO request,
            @AuthenticationPrincipal UserDetails principal) {

        StockMovementResponseDTO created =
                stockMovementService.recordMovement(request, principal.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(),
                        "Stock movement recorded successfully", created));
    }

    /**
     * ADMIN, STAFF — paginated stock-movement list with filters;
     * default sort is timestamp descending (Req 7.5).
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<PageResponseDTO<StockMovementResponseDTO>>> getMovements(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) MovementType movementType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "movementTimestamp"));
        PageResponseDTO<StockMovementResponseDTO> result =
                stockMovementService.getMovements(productId, movementType, startDate, endDate, userId, pageable);
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK.value(), "Stock movements retrieved", result));
    }
}
