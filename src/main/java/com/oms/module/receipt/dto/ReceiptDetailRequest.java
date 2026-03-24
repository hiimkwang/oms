package com.oms.module.receipt.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReceiptDetailRequest {
    @NotBlank(message = "Mã sản phẩm (SKU) không được để trống")
    private String sku;

    @Min(value = 1, message = "Số lượng nhập phải lớn hơn 0")
    private Integer quantity;

    @Min(value = 0, message = "Đơn giá nhập không được âm")
    private Double importPrice;
}