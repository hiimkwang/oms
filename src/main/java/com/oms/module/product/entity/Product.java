package com.oms.module.product.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sku", unique = true, nullable = false, length = 50)
    private String sku;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "brand", length = 100)
    private String brand;

    @Column(name = "condition_status", length = 50)
    private String conditionStatus;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Column(name = "min_stock_level", nullable = false)
    private Integer minStockLevel;

    @Column(name = "avg_import_price", precision = 15, scale = 2)
    private BigDecimal avgImportPrice;

    // ĐÃ ĐỔI TÊN THÀNH price
    @Column(name = "price", precision = 15, scale = 2)
    private BigDecimal price;

    @Column(name = "warranty_period", length = 50)
    private String warrantyPeriod;

    // ĐÃ ĐỔI TÊN THÀNH description
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariant> variants;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.stockQuantity == null) this.stockQuantity = 0;
        if (this.minStockLevel == null) this.minStockLevel = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}