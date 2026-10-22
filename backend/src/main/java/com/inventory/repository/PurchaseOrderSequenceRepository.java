package com.inventory.repository;

import com.inventory.entity.PurchaseOrderSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface PurchaseOrderSequenceRepository extends JpaRepository<PurchaseOrderSequence, LocalDate> {

    /**
     * Fetches the per-day counter row with a pessimistic write lock
     * (SELECT ... FOR UPDATE), guaranteeing serialized access when multiple
     * purchase orders are created on the same date concurrently.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM PurchaseOrderSequence s WHERE s.sequenceDate = :date")
    Optional<PurchaseOrderSequence> findByDateForUpdate(@Param("date") LocalDate date);
}
