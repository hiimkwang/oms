package com.oms.module.product.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductVariantRequest {
    private String variantName;
    private String sku;
    private BigDecimal price;        // Phải là BigDecimal
    private BigDecimal costPrice;    // Phải là BigDecimal
    private Integer stockQuantity;   // Phải là Integer
}