package com.inventory.service;

import com.inventory.dto.CreateProductRequestDTO;
import com.inventory.dto.PageResponseDTO;
import com.inventory.dto.ProductResponseDTO;
import com.inventory.dto.UpdateProductRequestDTO;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    ProductResponseDTO createProduct(CreateProductRequestDTO request);

    ProductResponseDTO updateProduct(Long id, UpdateProductRequestDTO request);

    ProductResponseDTO deactivateProduct(Long id);

    ProductResponseDTO getProductById(Long id);

    PageResponseDTO<ProductResponseDTO> getProducts(String name,
                                                    String sku,
                                                    Long categoryId,
                                                    Boolean active,
                                                    Pageable pageable);
}
