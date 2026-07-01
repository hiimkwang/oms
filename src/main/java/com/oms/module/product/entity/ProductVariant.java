package com.oms.module.product.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal; // Thêm dòng import này
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_variants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "variant_name", nullable = false)
    private String variantName;

    @Column(name = "sku", unique = true)
    private String sku;

    @Column(name = "price", precision = 15, scale = 2)
    private BigDecimal price;

    @Column(name = "cost_price", precision = 15, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    @org.hibernate.annotations.Formula("(SELECT COALESCE(SUM(i.stock), 0) FROM inventory i WHERE i.variant_id = id)")
    private Integer actualStock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private Product product;

    @JsonProperty("productName")
    public String getProductName() {
        if (this.product != null) {
            return this.product.getName();
        }
        return "Sản phẩm không xác định";
    }

    @Column(name = "image_url")
    private String imageUrl;

    // Mã vạch của biến thể: mỗi biến thể có thể có 1-nhiều barcode (tùy lô/nước) để quét khi đóng gói.
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "variant_barcodes", joinColumns = @JoinColumn(name = "variant_id"))
    @Column(name = "barcode", length = 100)
    @Builder.Default
    private List<String> barcodes = new ArrayList<>();
}