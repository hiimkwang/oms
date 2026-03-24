package com.oms.module.product.entity;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    private String sku; // Mã SP (VD: KB-AULA-F75-GB-RP-N)

    @Column(name = "name", nullable = false)
    private String name; // Sản phẩm dịch vụ

    @Column(name = "category", length = 100)
    private String category; // Loại sản phẩm (Bàn phím cơ, Phụ kiện...)

    @Column(name = "brand", length = 100)
    private String brand; // Hãng sản xuất (Aula, Leobog...)

    @Column(name = "condition_status", length = 50)
    private String conditionStatus; // Tình trạng (Mới (New), 2nd...)

    @Column(name = "unit", length = 20)
    private String unit; // ĐVT (Chiếc, Bộ...)

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity; // Số lượng tồn

    @Column(name = "min_stock_level", nullable = false)
    private Integer minStockLevel; // Định mức tồn tối thiểu

    @Column(name = "avg_import_price", precision = 15, scale = 2)
    private BigDecimal avgImportPrice; // Giá nhập bình quân

    @Column(name = "retail_price", precision = 15, scale = 2)
    private BigDecimal retailPrice; // Giá bán đề xuất

    @Column(name = "warranty_period", length = 50)
    private String warrantyPeriod; // Bảo hành (VD: 6 Tháng)

    @Column(name = "note", columnDefinition = "TEXT")
    private String note; // Ghi chú

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        // Mặc định khi tạo mới SP nếu không truyền số tồn thì gán = 0
        if (this.stockQuantity == null) this.stockQuantity = 0;
        if (this.minStockLevel == null) this.minStockLevel = 1;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}