package com.inventory.service;

import com.inventory.dto.CreateMovementRequestDTO;
import com.inventory.dto.PageResponseDTO;
import com.inventory.dto.StockMovementResponseDTO;
import com.inventory.entity.MovementType;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface StockMovementService {

    StockMovementResponseDTO recordMovement(CreateMovementRequestDTO request, String username);

    PageResponseDTO<StockMovementResponseDTO> getMovements(Long productId,
                                                           MovementType movementType,
                                                           LocalDateTime startDate,
                                                           LocalDateTime endDate,
                                                           Long userId,
                                                           Pageable pageable);
}
