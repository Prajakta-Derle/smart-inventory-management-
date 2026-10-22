package com.inventory.dto;

import com.inventory.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Purchase report over a date range (Requirement 10.1).
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseReportDTO {

    private long totalOrderCount;
    private BigDecimal totalAmount;
    private Map<OrderStatus, StatusBreakdown> breakdownByStatus;
    private List<PurchaseReportOrderDTO> orders;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusBreakdown {
        private long count;
        private BigDecimal totalAmount;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PurchaseReportOrderDTO {
        private String orderNumber;
        private String supplierName;
        private LocalDate orderDate;
        private OrderStatus status;
        private BigDecimal total;
    }
}
