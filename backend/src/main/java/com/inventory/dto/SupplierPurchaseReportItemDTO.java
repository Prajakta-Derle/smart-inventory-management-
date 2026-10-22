package com.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * One supplier's purchase aggregate for the supplier-purchase report
 * (Requirement 10.3).
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierPurchaseReportItemDTO {

    private Long supplierId;
    private String supplierName;
    private String companyName;
    private long purchaseOrderCount;
    private BigDecimal totalValue;
}
