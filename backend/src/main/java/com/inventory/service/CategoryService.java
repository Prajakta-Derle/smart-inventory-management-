package com.inventory.service;

import com.inventory.dto.CategoryProductDTO;
import com.inventory.dto.CategoryResponseDTO;
import com.inventory.dto.CreateCategoryRequestDTO;
import com.inventory.dto.PageResponseDTO;
import com.inventory.dto.UpdateCategoryRequestDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {

    CategoryResponseDTO createCategory(CreateCategoryRequestDTO request);

    CategoryResponseDTO updateCategory(Long id, UpdateCategoryRequestDTO request);

    CategoryResponseDTO deactivateCategory(Long id);

    List<CategoryResponseDTO> listCategories();

    PageResponseDTO<CategoryProductDTO> listProductsByCategory(Long categoryId, Pageable pageable);
}
