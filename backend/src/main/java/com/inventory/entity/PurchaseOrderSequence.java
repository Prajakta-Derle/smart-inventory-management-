package com.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Per-day counter backing the atomic generation of purchase order numbers
 * (format {@code PO-YYYYMMDD-NNNNNN}). The row for a given date is locked
 * with a pessimistic write lock inside the PO-creation transaction so that
 * concurrent inserts never produce duplicate order numbers (see design doc,
 * "Purchase Order — Order Number Generation").
 */
@Entity
@Table(name = "purchase_order_sequence")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderSequence {

    /** The calendar day this counter belongs to (also the primary key). */
    @Id
    @Column(name = "sequence_date", nullable = false)
    private LocalDate sequenceDate;

    /** Last-used sequence number for the day; next PO uses lastValue + 1. */
    @Column(name = "last_sequence_value", nullable = false)
    @Builder.Default
    private long lastValue = 0L;
}
