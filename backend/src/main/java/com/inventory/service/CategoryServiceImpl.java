package com.inventory.service;

import com.inventory.dto.CategoryProductDTO;
import com.inventory.dto.CategoryResponseDTO;
import com.inventory.dto.CreateCategoryRequestDTO;
import com.inventory.dto.PageResponseDTO;
import com.inventory.dto.UpdateCategoryRequestDTO;
import com.inventory.entity.Category;
import com.inventory.entity.Product;
import com.inventory.exception.DuplicateResourceException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    // ── Create (Req 4.1, 4.2) ─────────────────────────────────────────────────

    @Override
    @Transactional
    public CategoryResponseDTO createCategory(CreateCategoryRequestDTO request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("name", request.getName());
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .active(true)
                .build();

        Category saved = categoryRepository.save(category);
        return toResponseDTO(saved);
    }

    // ── Update (Req 4.3) ──────────────────────────────────────────────────────

    @Override
    @Transactional
    public CategoryResponseDTO updateCategory(Long id, UpdateCategoryRequestDTO request) {
        Category category = findCategoryOrThrow(id);

        // Reject a name clash with a *different* category (case-insensitive).
        categoryRepository.findByNameIgnoreCase(request.getName())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("name", request.getName());
                });

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        Category saved = categoryRepository.save(category);
        return toResponseDTO(saved);
    }

    // ── Deactivate / soft-delete (Req 4.4) ────────────────────────────────────

    @Override
    @Transactional
    public CategoryResponseDTO deactivateCategory(Long id) {
        Category category = findCategoryOrThrow(id);
        category.setActive(false);
        Category saved = categoryRepository.save(category);
        return toResponseDTO(saved);
    }

    // ── List all with product count (Req 4.6) ─────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> listCategories() {
        return categoryRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // ── List active products by category (Req 4.5, 4.7) ───────────────────────

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<CategoryProductDTO> listProductsByCategory(Long categoryId, Pageable pageable) {
        // 404 if the category does not exist (Req 4.7).
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category", categoryId);
        }

        Page<CategoryProductDTO> page = categoryRepository
                .findActiveProductsByCategoryId(categoryId, pageable)
                .map(this::toProductDTO);

        return PageResponseDTO.from(page);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Category findCategoryOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
    }

    private CategoryResponseDTO toResponseDTO(Category category) {
        return CategoryResponseDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .active(category.isActive())
                .productCount(categoryRepository.countActiveProductsByCategoryId(category.getId()))
                .createdAt(category.getCreatedAt())
                .build();
    }

    private CategoryProductDTO toProductDTO(Product product) {
        return CategoryProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .sku(product.getSku())
                .purchasePrice(product.getPurchasePrice())
                .sellingPrice(product.getSellingPrice())
                .currentStock(product.getCurrentStock())
                .unit(product.getUnit())
                .active(product.isActive())
                .build();
    }
}
