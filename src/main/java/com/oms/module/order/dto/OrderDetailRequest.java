package com.oms.module.order.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class OrderDetailRequest {

    @NotBlank(message = "Thiếu mã sản phẩm")
    private String sku;

    @NotBlank(message = "Thiếu tên sản phẩm")
    private String name;

    @NotNull
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer quantity;

    @NotNull(message = "Thiếu đơn giá")
    private BigDecimal unitPrice;

    private String note;
    private Boolean isCustom;
    private String serialNumber;
    private Integer warrantyMonths;
}