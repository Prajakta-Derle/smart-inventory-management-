package com.inventory.repository;

import com.inventory.entity.MovementType;
import com.inventory.entity.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    /**
     * Paginated stock-movement search with optional filters:
     * product ID, movement type, timestamp range, and acting user ID.
     * A null filter is ignored (Requirement 7.5). Sort order (default
     * timestamp descending) is supplied via the Pageable.
     */
    @Query("SELECT m FROM StockMovement m " +
           "WHERE (:productId IS NULL OR m.product.id = :productId) " +
           "AND (:movementType IS NULL OR m.movementType = :movementType) " +
           "AND (:startDate IS NULL OR m.movementTimestamp >= :startDate) " +
           "AND (:endDate IS NULL OR m.movementTimestamp <= :endDate) " +
           "AND (:userId IS NULL OR m.user.id = :userId)")
    Page<StockMovement> search(@Param("productId") Long productId,
                               @Param("movementType") MovementType movementType,
                               @Param("startDate") LocalDateTime startDate,
                               @Param("endDate") LocalDateTime endDate,
                               @Param("userId") Long userId,
                               Pageable pageable);

    // ── Dashboard queries (Requirement 9.2, 9.4) ──────────────────────────────

    /** Most recent movements, newest first (used for the recent-movements list). */
    java.util.List<StockMovement> findAllByOrderByMovementTimestampDesc(Pageable pageable);

    /**
     * Opening inventory value: signed SUM(quantity × unitCost) for all movements
     * strictly before {@code before} — ADD positive, REDUCE negative (Req 9.4).
     */
    @Query("SELECT COALESCE(SUM(CASE WHEN m.direction = com.inventory.entity.Direction.ADD " +
           "                         THEN m.quantity * m.unitCost " +
           "                         ELSE -(m.quantity * m.unitCost) END), 0) " +
           "FROM StockMovement m WHERE m.movementTimestamp < :before")
    java.math.BigDecimal sumSignedValueBefore(@Param("before") LocalDateTime before);

    /**
     * All movements with timestamp in [start, end], ordered ascending, used to
     * build per-day cumulative inventory values with each movement's recorded
     * unitCost (Req 9.4).
     */
    @Query("SELECT m FROM StockMovement m " +
           "WHERE m.movementTimestamp >= :start AND m.movementTimestamp <= :end " +
           "ORDER BY m.movementTimestamp ASC")
    java.util.List<StockMovement> findInRange(@Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end);

    // ── Reports (Requirement 10.2, 10.4) ──────────────────────────────────────

    /**
     * Movements in [start, end] matching an optional movement-type filter,
     * ordered by timestamp ascending (Req 10.4).
     */
    @Query("SELECT m FROM StockMovement m " +
           "WHERE m.movementTimestamp >= :start AND m.movementTimestamp <= :end " +
           "AND (:movementType IS NULL OR m.movementType = :movementType) " +
           "ORDER BY m.movementTimestamp ASC")
    java.util.List<StockMovement> findInRangeByType(@Param("start") LocalDateTime start,
                                                    @Param("end") LocalDateTime end,
                                                    @Param("movementType") MovementType movementType);

    /** Total quantity moved per movement type within [start, end]. Rows: {movementType, SUM(quantity)}. */
    @Query("SELECT m.movementType, COALESCE(SUM(m.quantity), 0) FROM StockMovement m " +
           "WHERE m.movementTimestamp >= :start AND m.movementTimestamp <= :end " +
           "GROUP BY m.movementType")
    java.util.List<Object[]> findQuantityTotalsByType(@Param("start") LocalDateTime start,
                                                      @Param("end") LocalDateTime end);

    /**
     * Per-product quantity sums grouped by movement type within [start, end],
     * used by the inventory report (Req 10.2). Rows: {productId, movementType, SUM(quantity)}.
     */
    @Query("SELECT m.product.id, m.movementType, COALESCE(SUM(m.quantity), 0) FROM StockMovement m " +
           "WHERE m.movementTimestamp >= :start AND m.movementTimestamp <= :end " +
           "GROUP BY m.product.id, m.movementType")
    java.util.List<Object[]> findProductQuantityByType(@Param("start") LocalDateTime start,
                                                       @Param("end") LocalDateTime end);
}
