package com.oms.module.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDTO {
    // Thông tin từ bảng Inventory
    private Long inventoryId;
    private Long branchId;
    private Integer stock;
    private Integer availableStock;

    // Thông tin từ bảng Product / ProductVariant
    private Long variantId;
    private String productName;
    private String variantName;
    private String sku;
    private String unit;
    private String imageUrl;
    private BigDecimal costPrice;
    private BigDecimal price;
    private LocalDateTime createdAt;
}