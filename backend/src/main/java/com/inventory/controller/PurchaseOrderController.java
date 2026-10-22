package com.inventory.controller;

import com.inventory.dto.ApiResponse;
import com.inventory.dto.CreatePurchaseOrderRequestDTO;
import com.inventory.dto.PageResponseDTO;
import com.inventory.dto.PurchaseOrderItemResponseDTO;
import com.inventory.dto.PurchaseOrderResponseDTO;
import com.inventory.dto.UpdateStatusRequestDTO;
import com.inventory.entity.OrderStatus;
import com.inventory.service.PurchaseOrderService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    /** Whitelist of sortable fields (Req 6.12) → entity property names. */
    private static final Map<String, String> SORT_WHITELIST = Map.of(
            "orderDate", "orderDate",
            "total", "total"
    );

    /**
     * ADMIN, STAFF — create a purchase order (DRAFT). Returns HTTP 201.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponseDTO>> createOrder(
            @Valid @RequestBody CreatePurchaseOrderRequestDTO request,
            @AuthenticationPrincipal UserDetails principal) {

        PurchaseOrderResponseDTO created =
                purchaseOrderService.createOrder(request, principal.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(),
                        "Purchase order created successfully", created));
    }

    /**
     * ADMIN, STAFF — paginated purchase-order list with filters and sort.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<PageResponseDTO<PurchaseOrderResponseDTO>>> getOrders(
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String orderNumber,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        String sortProperty = SORT_WHITELIST.getOrDefault(sortBy, "orderDate");
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir)
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortProperty));
        PageResponseDTO<PurchaseOrderResponseDTO> result =
                purchaseOrderService.getOrders(supplierId, status, startDate, endDate, orderNumber, pageable);
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK.value(), "Purchase orders retrieved", result));
    }

    /**
     * ADMIN, STAFF — get a purchase order by ID (404 if not found).
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponseDTO>> getOrderById(
            @PathVariable Long id) {

        PurchaseOrderResponseDTO order = purchaseOrderService.getOrderById(id);
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK.value(), "Purchase order retrieved", order));
    }

    /**
     * ADMIN, STAFF — update a purchase order's status (state machine enforced).
     * A RECEIVED transition atomically updates stock and records movements.
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponseDTO>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequestDTO request,
            @AuthenticationPrincipal UserDetails principal) {

        PurchaseOrderResponseDTO updated =
                purchaseOrderService.updateStatus(id, request, principal.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK.value(), "Purchase order status updated", updated));
    }

    /**
     * ADMIN, STAFF — list the line items of a purchase order.
     */
    @GetMapping("/{id}/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<PurchaseOrderItemResponseDTO>>> getOrderItems(
            @PathVariable Long id) {

        List<PurchaseOrderItemResponseDTO> items = purchaseOrderService.getOrderItems(id);
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK.value(), "Purchase order items retrieved", items));
    }
}
