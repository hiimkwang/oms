package com.oms.module.quotation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QuotationDetailRequest {
    @NotBlank(message = "Mã sản phẩm (SKU) không được để trống")
    private String sku;

    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer quantity;

    private Double unitPrice; // Cho phép sửa giá lúc báo
    private String warranty;
}