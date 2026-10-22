package com.inventory.repository;

import com.inventory.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    /**
     * Fetches a product with a pessimistic write lock (SELECT ... FOR UPDATE),
     * used by the transactional PO RECEIVED flow and manual stock movements to
     * serialize concurrent stock updates (Requirement 6.8).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

    /**
     * Paginated product search with optional filters:
     * partial (case-insensitive) name, partial (case-insensitive) SKU,
     * category ID, and active status. A null filter is ignored
     * (Requirement 3.6). Sorting is supplied via the Pageable.
     */
    @Query("SELECT p FROM Product p " +
           "WHERE (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:sku IS NULL OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :sku, '%'))) " +
           "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
           "AND (:active IS NULL OR p.active = :active)")
    Page<Product> search(@Param("name") String name,
                         @Param("sku") String sku,
                         @Param("categoryId") Long categoryId,
                         @Param("active") Boolean active,
                         Pageable pageable);

    // ── Low-stock queries (Requirement 8) ─────────────────────────────────────

    /**
     * Active products at or below their minimum stock level — includes
     * out-of-stock products (Req 8.3).
     */
    @Query("SELECT p FROM Product p " +
           "WHERE p.active = true AND p.currentStock <= p.minStockLevel")
    Page<Product> findLowOrOutOfStock(Pageable pageable);

    /**
     * Active products meeting the strict LOW_STOCK definition:
     * 0 &lt; currentStock &le; minStockLevel (Req 8.1, 8.6).
     */
    @Query("SELECT p FROM Product p " +
           "WHERE p.active = true AND p.currentStock > 0 AND p.currentStock <= p.minStockLevel")
    Page<Product> findLowStockOnly(Pageable pageable);

    /**
     * Active products that are out of stock: currentStock = 0 (Req 8.2, 8.4, 8.7).
     */
    @Query("SELECT p FROM Product p WHERE p.active = true AND p.currentStock = 0")
    Page<Product> findOutOfStock(Pageable pageable);

    /** Count of active LOW_STOCK products (0 &lt; stock &le; min). */
    @Query("SELECT COUNT(p) FROM Product p " +
           "WHERE p.active = true AND p.currentStock > 0 AND p.currentStock <= p.minStockLevel")
    long countLowStock();

    /** Count of active OUT_OF_STOCK products (stock = 0). */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.active = true AND p.currentStock = 0")
    long countOutOfStock();

    /**
     * Active low/out-of-stock products ordered by criticality for the summary
     * top-N list (Req 8.5): products with minStockLevel &gt; 0 first, ordered by
     * the ratio currentStock / minStockLevel ascending; products with
     * minStockLevel = 0 treated as ratio 0 and placed after the ratio-ranked
     * group; ties broken by product name ascending. Pagination is used to cap
     * the result to the top N.
     */
    @Query("SELECT p FROM Product p " +
           "WHERE p.active = true AND p.currentStock <= p.minStockLevel " +
           "ORDER BY " +
           "  CASE WHEN p.minStockLevel = 0 THEN 1 ELSE 0 END ASC, " +
           "  CASE WHEN p.minStockLevel = 0 THEN 0.0 " +
           "       ELSE (CAST(p.currentStock AS double) / p.minStockLevel) END ASC, " +
           "  p.name ASC")
    Page<Product> findTopCritical(Pageable pageable);

    // ── Dashboard aggregates (Requirement 9.1) ────────────────────────────────

    /** Count of active products. */
    long countByActiveTrue();

    /** Total inventory value: SUM(currentStock × purchasePrice) over active products. */
    @Query("SELECT COALESCE(SUM(p.currentStock * p.purchasePrice), 0) " +
           "FROM Product p WHERE p.active = true")
    java.math.BigDecimal sumInventoryValue();

    // ── Reports (Requirement 10.2, 10.5) ──────────────────────────────────────

    /** All active products, ordered by name (inventory report, Req 10.2). */
    @Query("SELECT p FROM Product p WHERE p.active = true ORDER BY p.name ASC")
    java.util.List<Product> findAllActive();

    /**
     * Active products at or below their minimum stock level, ordered by name —
     * list form used by the low-stock report (Req 10.5).
     */
    @Query("SELECT p FROM Product p " +
           "WHERE p.active = true AND p.currentStock <= p.minStockLevel ORDER BY p.name ASC")
    java.util.List<Product> findLowOrOutOfStockList();
}
