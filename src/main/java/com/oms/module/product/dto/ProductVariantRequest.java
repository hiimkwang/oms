package com.oms.module.product.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductVariantRequest {
    private String variantName;
    private String sku;
    private BigDecimal price;
    private BigDecimal costPrice;
    private Integer stockQuantity;
    private String imageUrl;
    private List<String> barcodes; // 1-nhiều mã vạch của biến thể
}