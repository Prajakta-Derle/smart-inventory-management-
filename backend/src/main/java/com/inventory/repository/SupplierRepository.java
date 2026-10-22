package com.inventory.repository;

import com.inventory.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Optional<Supplier> findByEmail(String email);

    boolean existsByEmail(String email);

    /**
     * Paginated list of suppliers with optional filters:
     * partial (case-insensitive) supplier name, partial company name,
     * and active status. A null filter is ignored (Requirement 5.5).
     */
    @Query("SELECT s FROM Supplier s " +
           "WHERE (:supplierName IS NULL OR LOWER(s.supplierName) LIKE LOWER(CONCAT('%', :supplierName, '%'))) " +
           "AND (:companyName IS NULL OR LOWER(s.companyName) LIKE LOWER(CONCAT('%', :companyName, '%'))) " +
           "AND (:active IS NULL OR s.active = :active)")
    Page<Supplier> search(@Param("supplierName") String supplierName,
                          @Param("companyName") String companyName,
                          @Param("active") Boolean active,
                          Pageable pageable);

    /**
     * Total number of purchase orders linked to a supplier
     * (populates SupplierResponseDTO.totalPurchaseOrderCount, Requirement 5.7).
     */
    @Query("SELECT COUNT(po) FROM PurchaseOrder po WHERE po.supplier.id = :supplierId")
    long countPurchaseOrdersBySupplierId(@Param("supplierId") Long supplierId);

    /** Count of active suppliers (Dashboard summary, Req 9.1). */
    long countByActiveTrue();
}
