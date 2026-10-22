package com.inventory.service;

import com.inventory.dto.DashboardSummaryDTO;
import com.inventory.dto.InventoryTrendPointDTO;
import com.inventory.dto.PurchaseTrendPointDTO;
import com.inventory.dto.StockMovementResponseDTO;
import com.inventory.entity.Direction;
import com.inventory.entity.OrderStatus;
import com.inventory.entity.Product;
import com.inventory.entity.StockMovement;
import com.inventory.entity.User;
import com.inventory.exception.InvalidDateRangeException;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.PurchaseOrderRepository;
import com.inventory.repository.StockMovementRepository;
import com.inventory.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final int RECENT_MOVEMENTS_LIMIT = 10;

    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final StockMovementRepository stockMovementRepository;

    // ── Summary KPIs (Req 9.1) ────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryDTO getSummary() {
        return DashboardSummaryDTO.builder()
                .totalActiveProducts(productRepository.countByActiveTrue())
                .totalActiveSuppliers(supplierRepository.countByActiveTrue())
                .totalInventoryValue(productRepository.sumInventoryValue())
                .totalPurchaseOrders(purchaseOrderRepository.count())
                .orderedPurchaseOrders(purchaseOrderRepository.countByStatus(OrderStatus.ORDERED))
                .lowStockCount(productRepository.countLowStock())
                .outOfStockCount(productRepository.countOutOfStock())
                .build();
    }

    // ── Recent movements (Req 9.2) ────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<StockMovementResponseDTO> getRecentMovements() {
        Pageable topTen = PageRequest.of(0, RECENT_MOVEMENTS_LIMIT);
        return stockMovementRepository
                .findAllByOrderByMovementTimestampDesc(topTen)
                .stream()
                .map(this::toMovementDTO)
                .toList();
    }

    // ── Purchase trend (Req 9.3) ──────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseTrendPointDTO> getPurchaseTrend(LocalDate start, LocalDate end) {
        validateRange(start, end);

        return purchaseOrderRepository.findDailyPurchaseTotals(start, end).stream()
                .map(row -> PurchaseTrendPointDTO.builder()
                        .date((LocalDate) row[0])
                        .totalAmount(row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO)
                        .build())
                .toList();
    }

    // ── Inventory trend (Req 9.4) ─────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<InventoryTrendPointDTO> getInventoryTrend(LocalDate start, LocalDate end) {
        validateRange(start, end);

        LocalDateTime startOfRange = start.atStartOfDay();
        LocalDateTime endOfRange = end.atTime(LocalTime.MAX);

        // 1. Opening value: all movements strictly before the start of the range.
        BigDecimal running = stockMovementRepository.sumSignedValueBefore(startOfRange);
        if (running == null) {
            running = BigDecimal.ZERO;
        }

        // 2. Movements within the range, grouped per day as we walk the calendar.
        List<StockMovement> movements = stockMovementRepository.findInRange(startOfRange, endOfRange);

        List<InventoryTrendPointDTO> result = new ArrayList<>();
        int idx = 0;
        for (LocalDate day = start; !day.isAfter(end); day = day.plusDays(1)) {
            // Apply every movement that occurred on this calendar day.
            while (idx < movements.size()
                    && movements.get(idx).getMovementTimestamp().toLocalDate().equals(day)) {
                running = running.add(signedValue(movements.get(idx)));
                idx++;
            }
            result.add(InventoryTrendPointDTO.builder()
                    .date(day)
                    .inventoryValue(running)
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

    /** Signed value of a movement: ADD positive, REDUCE negative, using recorded unitCost. */
    private BigDecimal signedValue(StockMovement m) {
        BigDecimal value = m.getUnitCost().multiply(BigDecimal.valueOf(m.getQuantity()));
        return m.getDirection() == Direction.ADD ? value : value.negate();
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
