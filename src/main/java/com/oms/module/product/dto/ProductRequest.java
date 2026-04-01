package com.oms.module.product.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductRequest {

    private String sku; // Để trống Spring Boot sẽ tự cho qua

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;

    private Long categoryId;
    private String brand;
    private String conditionStatus;
    private Boolean manageStock;
    private Long branchId;
    @NotBlank(message = "Đơn vị tính không được để trống")
    private String unit;

    private Integer minStockLevel = 0;
    private Integer stockQuantity = 0;

    // Đã đồng bộ tên
    private BigDecimal price;
    private String description;
    private String warrantyPeriod;
    private String imageUrl;
    private List<ProductVariantRequest> variants;
}