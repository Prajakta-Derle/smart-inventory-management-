package com.inventory.service;

import com.inventory.dto.LowStockItemDTO;
import com.inventory.dto.LowStockSummaryDTO;
import com.inventory.dto.PageResponseDTO;
import com.inventory.entity.Category;
import com.inventory.entity.Product;
import com.inventory.entity.StockStatus;
import com.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LowStockServiceImpl implements LowStockService {

    private static final int TOP_CRITICAL_LIMIT = 10;

    private final ProductRepository productRepository;

    // ── Low / out-of-stock list with optional status filter (Req 8.3, 8.6, 8.7)

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<LowStockItemDTO> getLowStockProducts(StockStatus filter, Pageable pageable) {
        Page<Product> page;
        if (filter == StockStatus.LOW_STOCK) {
            page = productRepository.findLowStockOnly(pageable);
        } else if (filter == StockStatus.OUT_OF_STOCK) {
            page = productRepository.findOutOfStock(pageable);
        } else {
            page = productRepository.findLowOrOutOfStock(pageable);
        }
        return PageResponseDTO.from(page.map(this::toItemDTO));
    }

    // ── Out-of-stock list (Req 8.4) ───────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<LowStockItemDTO> getOutOfStockProducts(Pageable pageable) {
        return PageResponseDTO.from(
                productRepository.findOutOfStock(pageable).map(this::toItemDTO));
    }

    // ── Summary + top-10 critical (Req 8.5) ───────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public LowStockSummaryDTO getLowStockSummary() {
        long lowStockCount = productRepository.countLowStock();
        long outOfStockCount = productRepository.countOutOfStock();

        Pageable topTen = PageRequest.of(0, TOP_CRITICAL_LIMIT, Sort.unsorted());
        List<LowStockItemDTO> topCritical = productRepository.findTopCritical(topTen)
                .map(this::toItemDTO)
                .getContent();

        return LowStockSummaryDTO.builder()
                .lowStockCount(lowStockCount)
                .outOfStockCount(outOfStockCount)
                .topCritical(topCritical)
                .build();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private LowStockItemDTO toItemDTO(Product product) {
        Category category = product.getCategory();
        return LowStockItemDTO.builder()
                .productId(product.getId())
                .productName(product.getName())
                .sku(product.getSku())
                .categoryName(category != null ? category.getName() : null)
                .currentStock(product.getCurrentStock())
                .minStockLevel(product.getMinStockLevel())
                .stockStatus(classify(product.getCurrentStock()))
                .build();
    }

    private StockStatus classify(int currentStock) {
        return currentStock == 0 ? StockStatus.OUT_OF_STOCK : StockStatus.LOW_STOCK;
    }
}
