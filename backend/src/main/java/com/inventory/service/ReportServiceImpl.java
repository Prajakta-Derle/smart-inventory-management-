package com.inventory.service;

import com.inventory.dto.InventoryReportItemDTO;
import com.inventory.dto.LowStockReportItemDTO;
import com.inventory.dto.PurchaseReportDTO;
import com.inventory.dto.StockMovementResponseDTO;
import com.inventory.dto.StockMovementReportDTO;
import com.inventory.dto.SupplierPurchaseReportItemDTO;
import com.inventory.entity.Category;
import com.inventory.entity.MovementType;
import com.inventory.entity.OrderStatus;
import com.inventory.entity.Product;
import com.inventory.entity.PurchaseOrder;
import com.inventory.entity.StockMovement;
import com.inventory.entity.StockStatus;
import com.inventory.entity.Supplier;
import com.inventory.entity.User;
import com.inventory.exception.InvalidDateRangeException;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.PurchaseOrderRepository;
import com.inventory.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;

    // ── Purchase report (Req 10.1) ────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PurchaseReportDTO getPurchaseReport(LocalDate start, LocalDate end) {
        validateRange(start, end);

        List<PurchaseOrder> orders = purchaseOrderRepository.findByOrderDateRange(start, end);

        Map<OrderStatus, PurchaseReportDTO.StatusBreakdown> breakdown = new LinkedHashMap<>();
        for (Object[] row : purchaseOrderRepository.findStatusBreakdown(start, end)) {
            OrderStatus status = (OrderStatus) row[0];
            long count = ((Number) row[1]).longValue();
            BigDecimal amount = row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO;
            breakdown.put(status, PurchaseReportDTO.StatusBreakdown.builder()
                    .count(count).totalAmount(amount).build());
        }

        BigDecimal totalAmount = orders.stream()
                .map(PurchaseOrder::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<PurchaseReportDTO.PurchaseReportOrderDTO> orderDtos = orders.stream()
                .map(po -> PurchaseReportDTO.PurchaseReportOrderDTO.builder()
                        .orderNumber(po.getOrderNumber())
                        .supplierName(po.getSupplier() != null ? po.getSupplier().getSupplierName() : null)
                        .orderDate(po.getOrderDate())
                        .status(po.getStatus())
                        .total(po.getTotal())
                        .build())
                .toList();

        return PurchaseReportDTO.builder()
                .totalOrderCount(orders.size())
                .totalAmount(totalAmount)
                .breakdownByStatus(breakdown)
                .orders(orderDtos)
                .build();
    }

    // ── Inventory report (Req 10.2) ───────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<InventoryReportItemDTO> getInventoryReport(LocalDate start, LocalDate end) {
        validateRange(start, end);

        LocalDateTime from = start.atStartOfDay();
        LocalDateTime to = end.atTime(LocalTime.MAX);

        // productId -> (movementType -> total quantity)
        Map<Long, Map<MovementType, Long>> perProduct = new HashMap<>();
        for (Object[] row : stockMovementRepository.findProductQuantityByType(from, to)) {
            Long productId = (Long) row[0];
            MovementType type = (MovementType) row[1];
            long qty = ((Number) row[2]).longValue();
            perProduct.computeIfAbsent(productId, k -> new EnumMap<>(MovementType.class))
                    .put(type, qty);
        }

        List<InventoryReportItemDTO> result = new ArrayList<>();
        for (Product p : productRepository.findAllActive()) {
            Map<MovementType, Long> byType = perProduct.getOrDefault(p.getId(), Map.of());
            result.add(InventoryReportItemDTO.builder()
                    .productId(p.getId())
                    .productName(p.getName())
                    .sku(p.getSku())
                    .currentStock(p.getCurrentStock())
                    .minStockLevel(p.getMinStockLevel())
                    .maxStockLevel(p.getMaxStockLevel())
                    .totalPurchaseQuantity(byType.getOrDefault(MovementType.PURCHASE, 0L))
                    .totalSaleQuantity(byType.getOrDefault(MovementType.SALE, 0L))
                    .totalDamagedQuantity(byType.getOrDefault(MovementType.DAMAGED, 0L))
                    .totalAdjustmentQuantity(byType.getOrDefault(MovementType.ADJUSTMENT, 0L))
                    .build());
        }
        return result;
    }

    // ── Supplier purchase report (Req 10.3) ───────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<SupplierPurchaseReportItemDTO> getSupplierPurchaseReport(LocalDate start, LocalDate end) {
        validateRange(start, end);

        List<SupplierPurchaseReportItemDTO> result = new ArrayList<>();
        for (Object[] row : purchaseOrderRepository.findSupplierPurchaseAggregates(start, end)) {
            result.add(SupplierPurchaseReportItemDTO.builder()
                    .supplierId((Long) row[0])
                    .supplierName((String) row[1])
                    .companyName((String) row[2])
                    .purchaseOrderCount(((Number) row[3]).longValue())
                    .totalValue(row[4] != null ? (BigDecimal) row[4] : BigDecimal.ZERO)
                    .build());
        }
        return result;
    }

    // ── Stock movement report (Req 10.4) ──────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public StockMovementReportDTO getStockMovementReport(LocalDate start, LocalDate end,
                                                         MovementType movementType) {
        validateRange(start, end);

        LocalDateTime from = start.atStartOfDay();
        LocalDateTime to = end.atTime(LocalTime.MAX);

        List<StockMovementResponseDTO> movements = stockMovementRepository
                .findInRangeByType(from, to, movementType).stream()
                .map(this::toMovementDTO)
                .toList();

        Map<MovementType, Long> totalsByType = new EnumMap<>(MovementType.class);
        for (Object[] row : stockMovementRepository.findQuantityTotalsByType(from, to)) {
            totalsByType.put((MovementType) row[0], ((Number) row[1]).longValue());
        }

        return StockMovementReportDTO.builder()
                .movements(movements)
                .totalQuantityByType(totalsByType)
                .build();
    }

    // ── Low stock report (Req 10.5) ───────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<LowStockReportItemDTO> getLowStockReport() {
        List<LowStockReportItemDTO> result = new ArrayList<>();
        for (Product p : productRepository.findLowOrOutOfStockList()) {
            Category category = p.getCategory();
            int reorder = p.getMaxStockLevel() - p.getCurrentStock();
            result.add(LowStockReportItemDTO.builder()
                    .productId(p.getId())
                    .productName(p.getName())
                    .sku(p.getSku())
                    .categoryName(category != null ? category.getName() : null)
                    .currentStock(p.getCurrentStock())
                    .minStockLevel(p.getMinStockLevel())
                    .maxStockLevel(p.getMaxStockLevel())
                    .stockStatus(p.getCurrentStock() == 0 ? StockStatus.OUT_OF_STOCK : StockStatus.LOW_STOCK)
                    .recommendedReorderQuantity(Math.max(reorder, 0))
                    .build());
        }
        return result;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void validateRange(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw new InvalidDateRangeException("Both start date and end date are required");
        }
        if (start.isAfter(end)) {
            throw new InvalidDateRangeException("Start date must not be after end date");
        }
    }

    private StockMovementResponseDTO toMovementDTO(StockMovement movement) {
        Product product = movement.getProduct();
        User user = movement.getUser();
        return StockMovementResponseDTO.builder()
                .id(movement.getId())
                .productId(product != null ? product.getId() : null)
                .productName(product != null ? product.getName() : null)
                .sku(product != null ? product.getSku() : null)
                .movementType(movement.getMovementType())
                .direction(movement.getDirection())
                .quantity(movement.getQuantity())
                .unitCost(movement.getUnitCost())
                .stockBefore(movement.getStockBefore())
                .stockAfter(movement.getStockAfter())
                .note(movement.getNote())
                .timestamp(movement.getMovementTimestamp())
                .actingUsername(user != null ? user.getUsername() : null)
                .build();
    }
}
