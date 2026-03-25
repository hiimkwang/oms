package com.oms.module.product.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal; // Thêm dòng import này

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

    // ĐỔI SANG BigDecimal
    @Column(name = "price", precision = 15, scale = 2)
    private BigDecimal price;

    // ĐỔI SANG BigDecimal
    @Column(name = "cost_price", precision = 15, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "stock_quantity")
    private Integer stockQuantity;

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
}