package com.inventory.service;

import com.inventory.dto.CreateMovementRequestDTO;
import com.inventory.dto.PageResponseDTO;
import com.inventory.dto.StockMovementResponseDTO;
import com.inventory.entity.Direction;
import com.inventory.entity.MovementType;
import com.inventory.entity.Product;
import com.inventory.entity.StockMovement;
import com.inventory.entity.User;
import com.inventory.exception.BusinessValidationException;
import com.inventory.exception.InsufficientStockException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.StockMovementRepository;
import com.inventory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class StockMovementServiceImpl implements StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // ── Record movement (Req 7.1–7.3, 7.7–7.9) ────────────────────────────────

    @Override
    @Transactional
    public StockMovementResponseDTO recordMovement(CreateMovementRequestDTO request, String username) {
        // ADJUSTMENT requires an explicit direction (Req 7.9).
        Direction direction = resolveDirection(request.getMovementType(), request.getDirection());

        User actingUser = loadUser(username);

        // Lock the product row to serialize concurrent stock updates.
        Product product = productRepository.findByIdForUpdate(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProductId()));

        // Product must be active (Req 7.1 — valid Product ID).
        if (!product.isActive()) {
            throw new BusinessValidationException(
                    "Product is inactive: " + request.getProductId());
        }

        int stockBefore = product.getCurrentStock();
        int signedDelta = (direction == Direction.ADD)
                ? request.getQuantity()
                : -request.getQuantity();
        int stockAfter = stockBefore + signedDelta;

        // Stock may never go negative (Req 7.2).
        if (stockAfter < 0) {
            throw new InsufficientStockException(
                    product.getName(), stockBefore, request.getQuantity());
        }

        StockMovement movement = StockMovement.builder()
                .product(product)
                .user(actingUser)
                .movementType(request.getMovementType())
                .direction(direction)
                .quantity(request.getQuantity())
                .unitCost(product.getPurchasePrice())   // captured at movement time (Req 7.1)
                .stockBefore(stockBefore)
                .stockAfter(stockAfter)
                .note(request.getNote())
                .movementTimestamp(LocalDateTime.now(ZoneOffset.UTC))
                .build();

        StockMovement saved = stockMovementRepository.save(movement);

        product.setCurrentStock(stockAfter);
        productRepository.save(product);

        return toResponseDTO(saved);
    }

    // ── Paginated list with filters (Req 7.5) ─────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<StockMovementResponseDTO> getMovements(Long productId,
                                                                  MovementType movementType,
                                                                  LocalDateTime startDate,
                                                                  LocalDateTime endDate,
                                                                  Long userId,
                                                                  Pageable pageable) {
        Page<StockMovementResponseDTO> page = stockMovementRepository
                .search(productId, movementType, startDate, endDate, userId, pageable)
                .map(this::toResponseDTO);
        return PageResponseDTO.from(page);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Derives the movement direction (Req 7.1 / 7.9):
     * PURCHASE → ADD; SALE, DAMAGED → REDUCE; ADJUSTMENT → the explicit
     * direction from the request (rejected with 400 if absent).
     */
    private Direction resolveDirection(MovementType type, Direction requested) {
        return switch (type) {
            case PURCHASE -> Direction.ADD;
            case SALE, DAMAGED -> Direction.REDUCE;
            case ADJUSTMENT -> {
                if (requested == null) {
                    throw new BusinessValidationException(
                            "Direction (ADD or REDUCE) is required for ADJUSTMENT movements");
                }
                yield requested;
            }
        };
    }

    private User loadUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));
    }

    private StockMovementResponseDTO toResponseDTO(StockMovement movement) {
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
