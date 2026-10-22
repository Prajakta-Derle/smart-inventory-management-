package com.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "products",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_products_sku", columnNames = "sku")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String sku;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_products_category"))
    private Category category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "purchase_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal purchasePrice;

    @Column(name = "selling_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal sellingPrice;

    @Column(name = "current_stock", nullable = false)
    @Builder.Default
    private int currentStock = 0;

    @Column(name = "min_stock_level", nullable = false)
    private int minStockLevel;

    @Column(name = "max_stock_level", nullable = false)
    private int maxStockLevel;

    @Column(nullable = false, length = 50)
    private String unit;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<StockMovement> stockMovements;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<PurchaseOrderItem> purchaseOrderItems;
}
