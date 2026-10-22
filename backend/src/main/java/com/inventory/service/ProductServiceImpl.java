package com.inventory.service;

import com.inventory.dto.CreateProductRequestDTO;
import com.inventory.dto.PageResponseDTO;
import com.inventory.dto.ProductResponseDTO;
import com.inventory.dto.UpdateProductRequestDTO;
import com.inventory.entity.Category;
import com.inventory.entity.Product;
import com.inventory.exception.DuplicateResourceException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.CategoryRepository;
import com.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    // ── Create (Req 3.1, 3.2) ─────────────────────────────────────────────────

    @Override
    @Transactional
    public ProductResponseDTO createProduct(CreateProductRequestDTO request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("sku", request.getSku());
        }

        Category category = findCategoryOrThrow(request.getCategoryId());

        Product product = Product.builder()
                .name(request.getName())
                .sku(request.getSku())
                .category(category)
                .description(request.getDescription())
                .purchasePrice(request.getPurchasePrice())
                .sellingPrice(request.getSellingPrice())
                .currentStock(0)                 // initial stock always zero (Req 3.1)
                .minStockLevel(request.getMinStockLevel())
                .maxStockLevel(request.getMaxStockLevel())
                .unit(request.getUnit())
                .active(true)
                .build();

        Product saved = productRepository.save(product);
        return toResponseDTO(saved);
    }

    // ── Update (Req 3.3, 3.4) ─────────────────────────────────────────────────

    @Override
    @Transactional
    public ProductResponseDTO updateProduct(Long id, UpdateProductRequestDTO request) {
        Product product = findProductOrThrow(id);

        // Reject an SKU clash with a *different* product (Req 3.4).
        productRepository.findBySku(request.getSku())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("sku", request.getSku());
                });

        Category category = findCategoryOrThrow(request.getCategoryId());

        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setCategory(category);
        product.setDescription(request.getDescription());
        product.setPurchasePrice(request.getPurchasePrice());
        product.setSellingPrice(request.getSellingPrice());
        product.setMinStockLevel(request.getMinStockLevel());
        product.setMaxStockLevel(request.getMaxStockLevel());
        product.setUnit(request.getUnit());
        // currentStock is never modified here — only via stock movements / PO receipt.

        Product saved = productRepository.save(product);
        return toResponseDTO(saved);
    }

    // ── Deactivate / soft-delete (Req 3.5) ────────────────────────────────────

    @Override
    @Transactional
    public ProductResponseDTO deactivateProduct(Long id) {
        Product product = findProductOrThrow(id);
        product.setActive(false);
        Product saved = productRepository.save(product);
        return toResponseDTO(saved);
    }

    // ── Get by ID (Req 3.7) ───────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDTO getProductById(Long id) {
        return toResponseDTO(findProductOrThrow(id));
    }

    // ── Paginated list with filters/sort (Req 3.6) ────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<ProductResponseDTO> getProducts(String name,
                                                           String sku,
                                                           Long categoryId,
                                                           Boolean active,
                                                           Pageable pageable) {
        Page<ProductResponseDTO> page = productRepository
                .search(normalize(name), normalize(sku), categoryId, active, pageable)
                .map(this::toResponseDTO);
        return PageResponseDTO.from(page);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Product findProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    private Category findCategoryOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private ProductResponseDTO toResponseDTO(Product product) {
        Category category = product.getCategory();
        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .sku(product.getSku())
                .categoryId(category != null ? category.getId() : null)
                .categoryName(category != null ? category.getName() : null)
                .description(product.getDescription())
                .purchasePrice(product.getPurchasePrice())
                .sellingPrice(product.getSellingPrice())
                .currentStock(product.getCurrentStock())
                .minStockLevel(product.getMinStockLevel())
                .maxStockLevel(product.getMaxStockLevel())
                .unit(product.getUnit())
                .active(product.isActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
