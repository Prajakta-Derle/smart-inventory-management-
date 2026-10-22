package com.inventory.repository;

import com.inventory.entity.OrderStatus;
import com.inventory.entity.PurchaseOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    /**
     * Paginated purchase-order search with optional filters:
     * supplier ID, status, order-date range, and partial (case-insensitive)
     * order number. A null filter is ignored (Requirement 6.12).
     * Sorting (order date / total) is supplied via the Pageable.
     */
    @Query("SELECT po FROM PurchaseOrder po " +
           "WHERE (:supplierId IS NULL OR po.supplier.id = :supplierId) " +
           "AND (:status IS NULL OR po.status = :status) " +
           "AND (:startDate IS NULL OR po.orderDate >= :startDate) " +
           "AND (:endDate IS NULL OR po.orderDate <= :endDate) " +
           "AND (:orderNumber IS NULL OR LOWER(po.orderNumber) LIKE LOWER(CONCAT('%', :orderNumber, '%')))")
    Page<PurchaseOrder> search(@Param("supplierId") Long supplierId,
                               @Param("status") OrderStatus status,
                               @Param("startDate") LocalDate startDate,
                               @Param("endDate") LocalDate endDate,
                               @Param("orderNumber") String orderNumber,
                               Pageable pageable);

    // ── Dashboard aggregates (Requirement 9.1, 9.3) ───────────────────────────

    /** Count of purchase orders in a given status (e.g. ORDERED). */
    long countByStatus(OrderStatus status);

    /**
     * Daily purchase-order totals within [start, end] for orders in status
     * ORDERED or RECEIVED (Req 9.3). Each row is {orderDate, SUM(total)}.
     */
    @Query("SELECT po.orderDate, SUM(po.total) FROM PurchaseOrder po " +
           "WHERE po.orderDate >= :start AND po.orderDate <= :end " +
           "AND po.status IN (com.inventory.entity.OrderStatus.ORDERED, " +
           "                  com.inventory.entity.OrderStatus.RECEIVED) " +
           "GROUP BY po.orderDate ORDER BY po.orderDate ASC")
    java.util.List<Object[]> findDailyPurchaseTotals(@Param("start") LocalDate start,
                                                     @Param("end") LocalDate end);

    // ── Reports (Requirement 10.1, 10.3) ──────────────────────────────────────

    /** All purchase orders whose order date falls within [start, end], ordered by date. */
    @Query("SELECT po FROM PurchaseOrder po " +
           "WHERE po.orderDate >= :start AND po.orderDate <= :end " +
           "ORDER BY po.orderDate ASC")
    java.util.List<PurchaseOrder> findByOrderDateRange(@Param("start") LocalDate start,
                                                       @Param("end") LocalDate end);

    /** Purchase-order count and summed total grouped by status within [start, end]. */
    @Query("SELECT po.status, COUNT(po), COALESCE(SUM(po.total), 0) FROM PurchaseOrder po " +
           "WHERE po.orderDate >= :start AND po.orderDate <= :end " +
           "GROUP BY po.status")
    java.util.List<Object[]> findStatusBreakdown(@Param("start") LocalDate start,
                                                 @Param("end") LocalDate end);

    /**
     * Per-supplier purchase-order count and total value within [start, end],
     * ordered by total value descending (Req 10.3). Each row is
     * {supplierId, supplierName, companyName, count, totalValue}.
     */
    @Query("SELECT s.id, s.supplierName, s.companyName, COUNT(po), COALESCE(SUM(po.total), 0) " +
           "FROM PurchaseOrder po JOIN po.supplier s " +
           "WHERE po.orderDate >= :start AND po.orderDate <= :end " +
           "GROUP BY s.id, s.supplierName, s.companyName " +
           "ORDER BY COALESCE(SUM(po.total), 0) DESC")
    java.util.List<Object[]> findSupplierPurchaseAggregates(@Param("start") LocalDate start,
                                                            @Param("end") LocalDate end);
}
