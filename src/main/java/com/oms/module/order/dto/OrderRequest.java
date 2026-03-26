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

    private String customerCode; // Nếu dùng ID thì đổi tên thành customerId
    private String salesChannel;
    private String paymentMethod;
    private String status = "Khởi tạo";
    private String note;
    private Double discount;      // Giảm giá tổng đơn
    private Double shippingFee;   // Phí vận chuyển
    private String createdAt;     // Ngày đặt đơn từ giao diện

    @NotEmpty(message = "Đơn hàng phải có ít nhất 1 sản phẩm")
    @Valid
    private List<OrderDetailRequest> details; // Đổi từ 'items' thành 'details' để khớp logic hiện tại
}