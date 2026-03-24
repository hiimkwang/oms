package com.oms.module.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {

    @NotBlank(message = "Mã SP (SKU) không được để trống")
    private String sku;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;

    private String category;
    private String brand;
    private String conditionStatus;

    @NotBlank(message = "Đơn vị tính không được để trống")
    private String unit;

    @NotNull(message = "Định mức tồn tối thiểu không được để trống")
    @Min(value = 0, message = "Định mức tồn không được âm")
    private Integer minStockLevel;

    private BigDecimal retailPrice;
    private String warrantyPeriod;
    private String note;
}