package com.oms.module.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    @NotBlank(message = "Mã đơn hàng không được để trống")
    private String orderCode;

    @NotBlank(message = "Mã khách hàng không được để trống")
    private String customerCode;

    private String salesChannel;
    private String paymentMethod;
    private String status = "Khởi tạo";
    private String note;

    @NotEmpty(message = "Đơn hàng phải có ít nhất 1 sản phẩm")
    @Valid
    private List<OrderDetailRequest> details;
}