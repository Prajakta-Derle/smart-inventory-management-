package com.inventory.service;

import com.inventory.dto.CreatePurchaseOrderRequestDTO;
import com.inventory.dto.PageResponseDTO;
import com.inventory.dto.PurchaseOrderItemRequestDTO;
import com.inventory.dto.PurchaseOrderItemResponseDTO;
import com.inventory.dto.PurchaseOrderResponseDTO;
import com.inventory.dto.UpdateStatusRequestDTO;
import com.inventory.entity.Direction;
import com.inventory.entity.MovementType;
import com.inventory.entity.OrderStatus;
import com.inventory.entity.Product;
import com.inventory.entity.PurchaseOrder;
import com.inventory.entity.PurchaseOrderItem;
import com.inventory.entity.PurchaseOrderSequence;
import com.inventory.entity.StockMovement;
import com.inventory.entity.Supplier;
import com.inventory.entity.User;
import com.inventory.exception.BusinessValidationException;
import com.inventory.exception.InvalidDateRangeException;
import com.inventory.exception.InvalidStatusTransitionException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.PurchaseOrderRepository;
import com.inventory.repository.PurchaseOrderSequenceRepository;
import com.inventory.repository.StockMovementRepository;
import com.inventory.repository.SupplierRepository;
import com.inventory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private static final DateTimeFormatter ORDER_NUMBER_DATE =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderSequenceRepository sequenceRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final StockMovementRepository stockMovementRepository;
    private final UserRepository userRepository;

    // ── Create (Req 6.1–6.5) ──────────────────────────────────────────────────

    @Override
    @Transactional
    public PurchaseOrderResponseDTO createOrder(CreatePurchaseOrderRequestDTO request, String username) {
        // Supplier must exist and be active (Req 6.2).
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new BusinessValidationException(
                        "Supplier does not exist: " + request.getSupplierId()));
        if (!supplier.isActive()) {
            throw new BusinessValidationException(
                    "Supplier is inactive: " + request.getSupplierId());
        }

        // Delivery date must be on or after order date (Req 6.4).
        if (request.getExpectedDeliveryDate().isBefore(request.getOrderDate())) {
            throw new InvalidDateRangeException(
                    "Expected delivery date must not be before the order date");
        }

        // Reject duplicate product IDs across items (Req 6.5).
        Set<Long> seenProductIds = new HashSet<>();
        for (PurchaseOrderItemRequestDTO item : request.getItems()) {
            if (!seenProductIds.add(item.getProductId())) {
                throw new BusinessValidationException(
                        "Duplicate product in order items: " + item.getProductId());
            }
        }

        User actingUser = loadUser(username);

        PurchaseOrder order = PurchaseOrder.builder()
                .orderNumber(generateOrderNumber(request.getOrderDate()))
                .supplier(supplier)
                .createdBy(actingUser)
                .orderDate(request.getOrderDate())
                .expectedDeliveryDate(request.getExpectedDeliveryDate())
                .status(OrderStatus.DRAFT)
                .items(new ArrayList<>())
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseOrderItemRequestDTO itemReq : request.getItems()) {
            // Each product must exist and be active (Req 6.3).
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new BusinessValidationException(
                            "Product does not exist: " + itemReq.getProductId()));
            if (!product.isActive()) {
                throw new BusinessValidationException(
                        "Product is inactive: " + itemReq.getProductId());
            }

            BigDecimal subtotal = itemReq.getPurchasePrice()
                    .multiply(BigDecimal.valueOf(itemReq.getQuantity()));

            PurchaseOrderItem item = PurchaseOrderItem.builder()
                    .purchaseOrder(order)
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .purchasePrice(itemReq.getPurchasePrice())
                    .subtotal(subtotal)
                    .build();

            order.getItems().add(item);
            total = total.add(subtotal);
        }

        order.setTotal(total);
        PurchaseOrder saved = purchaseOrderRepository.save(order);
        return toResponseDTO(saved);
    }

    // ── Status transition (Req 6.6, 6.7, 6.10, 6.11) + RECEIVED flow (6.8, 6.9) ─

    @Override
    @Transactional
    public PurchaseOrderResponseDTO updateStatus(Long id, UpdateStatusRequestDTO request, String username) {
        PurchaseOrder order = findOrderOrThrow(id);
        OrderStatus current = order.getStatus();
        OrderStatus target = request.getStatus();

        validateTransition(current, target);

        if (target == OrderStatus.RECEIVED) {
            applyReceivedTransition(order, loadUser(username));
        }

        order.setStatus(target);
        PurchaseOrder saved = purchaseOrderRepository.save(order);
        return toResponseDTO(saved);
    }

    /**
     * Validates the PO status state machine:
     * DRAFT → ORDERED | CANCELLED; ORDERED → RECEIVED | CANCELLED.
     * Every other transition is rejected with HTTP 400.
     */
    private void validateTransition(OrderStatus current, OrderStatus target) {
        boolean valid =
                (current == OrderStatus.DRAFT &&
                        (target == OrderStatus.ORDERED || target == OrderStatus.CANCELLED))
             || (current == OrderStatus.ORDERED &&
                        (target == OrderStatus.RECEIVED || target == OrderStatus.CANCELLED));

        if (!valid) {
            throw new InvalidStatusTransitionException(current.name(), target.name());
        }
    }

    /**
     * Transactional RECEIVED flow (Req 6.8): for each item, lock the product
     * row, capture stock-before, increment stock, create an immutable
     * PURCHASE/ADD StockMovement, and update the product's current stock.
     * Any failure rolls back the whole transaction (handled by @Transactional).
     */
    private void applyReceivedTransition(PurchaseOrder order, User actingUser) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        for (PurchaseOrderItem item : order.getItems()) {
            Product product = productRepository.findByIdForUpdate(item.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product", item.getProduct().getId()));

            int stockBefore = product.getCurrentStock();
            int stockAfter = stockBefore + item.getQuantity();

            StockMovement movement = StockMovement.builder()
                    .product(product)
                    .user(actingUser)
                    .movementType(MovementType.PURCHASE)
                    .direction(Direction.ADD)
                    .quantity(item.getQuantity())
                    .unitCost(item.getPurchasePrice())
                    .stockBefore(stockBefore)
                    .stockAfter(stockAfter)
                    .note(order.getOrderNumber())
                    .movementTimestamp(now)
                    .build();
            stockMovementRepository.save(movement);

            product.setCurrentStock(stockAfter);
            productRepository.save(product);
        }
    }

    // ── Read operations (Req 6.12, 6.13, 6.15) ────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PurchaseOrderResponseDTO getOrderById(Long id) {
        return toResponseDTO(findOrderOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrderItemResponseDTO> getOrderItems(Long id) {
        PurchaseOrder order = findOrderOrThrow(id);
        return order.getItems().stream().map(this::toItemResponseDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<PurchaseOrderResponseDTO> getOrders(Long supplierId,
                                                               OrderStatus status,
                                                               LocalDate startDate,
                                                               LocalDate endDate,
                                                               String orderNumber,
                                                               Pageable pageable) {
        Page<PurchaseOrderResponseDTO> page = purchaseOrderRepository
                .search(supplierId, status, startDate, endDate, normalize(orderNumber), pageable)
                .map(this::toResponseDTO);
        return PageResponseDTO.from(page);
    }

    // ── Order-number generation (atomic, per-day) ─────────────────────────────

    private String generateOrderNumber(LocalDate orderDate) {
        PurchaseOrderSequence sequence = sequenceRepository.findByDateForUpdate(orderDate)
                .orElseGet(() -> PurchaseOrderSequence.builder()
                        .sequenceDate(orderDate)
                        .lastValue(0L)
                        .build());

        long next = sequence.getLastValue() + 1;
        sequence.setLastValue(next);
        sequenceRepository.save(sequence);

        return String.format("PO-%s-%06d", orderDate.format(ORDER_NUMBER_DATE), next);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private PurchaseOrder findOrderOrThrow(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", id));
    }

    private User loadUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private PurchaseOrderResponseDTO toResponseDTO(PurchaseOrder order) {
        Supplier supplier = order.getSupplier();
        List<PurchaseOrderItemResponseDTO> items = order.getItems() == null
                ? List.of()
                : order.getItems().stream().map(this::toItemResponseDTO).toList();

        return PurchaseOrderResponseDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .supplierId(supplier != null ? supplier.getId() : null)
                .supplierName(supplier != null ? supplier.getSupplierName() : null)
                .orderDate(order.getOrderDate())
                .expectedDeliveryDate(order.getExpectedDeliveryDate())
                .status(order.getStatus())
                .total(order.getTotal())
                .items(items)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private PurchaseOrderItemResponseDTO toItemResponseDTO(PurchaseOrderItem item) {
        Product product = item.getProduct();
        return PurchaseOrderItemResponseDTO.builder()
                .id(item.getId())
                .productId(product != null ? product.getId() : null)
                .productName(product != null ? product.getName() : null)
                .sku(product != null ? product.getSku() : null)
                .quantity(item.getQuantity())
                .purchasePrice(item.getPurchasePrice())
                .subtotal(item.getSubtotal())
                .build();
    }
}
