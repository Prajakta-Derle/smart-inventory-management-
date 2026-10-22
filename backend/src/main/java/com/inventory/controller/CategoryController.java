package com.inventory.controller;

import com.inventory.dto.ApiResponse;
import com.inventory.dto.CategoryProductDTO;
import com.inventory.dto.CategoryResponseDTO;
import com.inventory.dto.CreateCategoryRequestDTO;
import com.inventory.dto.PageResponseDTO;
import com.inventory.dto.UpdateCategoryRequestDTO;
import com.inventory.service.CategoryService;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * ADMIN only — create a category. Returns HTTP 201.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> createCategory(
            @Valid @RequestBody CreateCategoryRequestDTO request) {

        CategoryResponseDTO created = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(),
                        "Category created successfully", created));
    }

    /**
     * ADMIN, STAFF — list all categories with product count.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<CategoryResponseDTO>>> listCategories() {

        List<CategoryResponseDTO> categories = categoryService.listCategories();
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK.value(), "Categories retrieved", categories));
    }

    /**
     * ADMIN only — update a category. Returns HTTP 200.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequestDTO request) {

        CategoryResponseDTO updated = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK.value(), "Category updated successfully", updated));
    }

    /**
     * ADMIN only — soft-delete (deactivate) a category. Returns HTTP 200.
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> deactivateCategory(
            @PathVariable Long id) {

        CategoryResponseDTO deactivated = categoryService.deactivateCategory(id);
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK.value(), "Category deactivated successfully", deactivated));
    }

    /**
     * ADMIN, STAFF — paginated list of active products in a category.
     */
    @GetMapping("/{id}/products")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<PageResponseDTO<CategoryProductDTO>>> listProductsByCategory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        PageResponseDTO<CategoryProductDTO> result =
                categoryService.listProductsByCategory(id, pageable);
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK.value(), "Products retrieved", result));
    }
}
