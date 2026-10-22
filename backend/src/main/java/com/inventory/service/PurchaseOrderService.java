package com.inventory.service;

import com.inventory.dto.CreatePurchaseOrderRequestDTO;
import com.inventory.dto.PageResponseDTO;
import com.inventory.dto.PurchaseOrderItemResponseDTO;
import com.inventory.dto.PurchaseOrderResponseDTO;
import com.inventory.dto.UpdateStatusRequestDTO;
import com.inventory.entity.OrderStatus;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface PurchaseOrderService {

    PurchaseOrderResponseDTO createOrder(CreatePurchaseOrderRequestDTO request, String username);

    PurchaseOrderResponseDTO updateStatus(Long id, UpdateStatusRequestDTO request, String username);

    PurchaseOrderResponseDTO getOrderById(Long id);

    List<PurchaseOrderItemResponseDTO> getOrderItems(Long id);

    PageResponseDTO<PurchaseOrderResponseDTO> getOrders(Long supplierId,
                                                        OrderStatus status,
                                                        LocalDate startDate,
                                                        LocalDate endDate,
                                                        String orderNumber,
                                                        Pageable pageable);
}
