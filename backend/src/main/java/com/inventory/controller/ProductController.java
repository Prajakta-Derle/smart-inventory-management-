package com.inventory.controller;

import com.inventory.dto.ApiResponse;
import com.inventory.dto.CreateProductRequestDTO;
import com.inventory.dto.PageResponseDTO;
import com.inventory.dto.ProductResponseDTO;
import com.inventory.dto.UpdateProductRequestDTO;
import com.inventory.service.ProductService;
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

import java.util.Map;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /** Whitelist of sortable fields (Req 3.6) → entity property names. */
    private static final Map<String, String> SORT_WHITELIST = Map.of(
            "name", "name",
            "sku", "sku",
            "currentStock", "currentStock",
            "createdAt", "createdAt"
    );

    /**
     * ADMIN, STAFF — create a product. Initial stock is always 0. Returns HTTP 201.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> createProduct(
            @Valid @RequestBody CreateProductRequestDTO request) {

        ProductResponseDTO created = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(),
                        "Product created successfully", created));
    }

    /**
     * ADMIN, STAFF — paginated product list with search / filter / sort.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<PageResponseDTO<ProductResponseDTO>>> getProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc")  String sortDir) {

        String sortProperty = SORT_WHITELIST.getOrDefault(sortBy, "name");
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir)
                ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortProperty));
        PageResponseDTO<ProductResponseDTO> result =
                productService.getProducts(name, sku, categoryId, active, pageable);
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK.value(), "Products retrieved", result));
    }

    /**
     * ADMIN, STAFF — get a single product by ID (404 if not found).
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> getProductById(
            @PathVariable Long id) {

        ProductResponseDTO product = productService.getProductById(id);
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK.value(), "Product retrieved", product));
    }

    /**
     * ADMIN, STAFF — update a product. Returns HTTP 200.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequestDTO request) {

        ProductResponseDTO updated = productService.updateProduct(id, request);
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK.value(), "Product updated successfully", updated));
    }

    /**
     * ADMIN only — soft-delete (deactivate) a product. Returns HTTP 200.
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> deactivateProduct(
            @PathVariable Long id) {

        ProductResponseDTO deactivated = productService.deactivateProduct(id);
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK.value(), "Product deactivated successfully", deactivated));
    }
}
