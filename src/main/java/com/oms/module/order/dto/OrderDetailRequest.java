package com.oms.module.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderDetailRequest {
    @NotBlank(message = "Mã sản phẩm (SKU) không được để trống")
    private String sku;

    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer quantity;
    private Double unitPrice;
    private Double discount = 0.0;
}