package com.inventory.repository;

import com.inventory.entity.Category;
import com.inventory.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    /**
     * Number of active products belonging to the given category.
     * Used to populate CategoryResponseDTO.productCount.
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId AND p.active = true")
    long countActiveProductsByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * Paginated list of active products belonging to the given category
     * (Requirement 4.5).
     */
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.active = true")
    Page<Product> findActiveProductsByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);
}
