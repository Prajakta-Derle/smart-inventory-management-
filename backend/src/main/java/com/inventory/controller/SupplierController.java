package com.inventory.controller;

import com.inventory.dto.ApiResponse;
import com.inventory.dto.CreateSupplierRequestDTO;
import com.inventory.dto.PageResponseDTO;
import com.inventory.dto.SupplierResponseDTO;
import com.inventory.dto.UpdateSupplierRequestDTO;
import com.inventory.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    /**
     * ADMIN only — create a supplier. Returns HTTP 201.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SupplierResponseDTO>> createSupplier(
            @Valid @RequestBody CreateSupplierRequestDTO request) {

        SupplierResponseDTO created = supplierService.createSupplier(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(),
                        "Supplier created successfully", created));
    }

    /**
     * ADMIN, STAFF — paginated supplier list with optional
     * name / company / active-status filters.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<PageResponseDTO<SupplierResponseDTO>>> listSuppliers(
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("supplierName").ascending());
        PageResponseDTO<SupplierResponseDTO> result =
                supplierService.listSuppliers(supplierName, companyName, active, pageable);
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK.value(), "Suppliers retrieved", result));
    }

    /**
     * ADMIN, STAFF — get a single supplier by ID (404 if not found).
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<SupplierResponseDTO>> getSupplierById(
            @PathVariable Long id) {

        SupplierResponseDTO supplier = supplierService.getSupplierById(id);
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK.value(), "Supplier retrieved", supplier));
    }

    /**
     * ADMIN only — update a supplier. Returns HTTP 200.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SupplierResponseDTO>> updateSupplier(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSupplierRequestDTO request) {

        SupplierResponseDTO updated = supplierService.updateSupplier(id, request);
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK.value(), "Supplier updated successfully", updated));
    }

    /**
     * ADMIN only — soft-delete (deactivate) a supplier. Returns HTTP 200.
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SupplierResponseDTO>> deactivateSupplier(
            @PathVariable Long id) {

        SupplierResponseDTO deactivated = supplierService.deactivateSupplier(id);
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK.value(), "Supplier deactivated successfully", deactivated));
    }
}
