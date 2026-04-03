package com.oms.module.order.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderRequest {

    @NotBlank(message = "Thiếu thông tin khách hàng")
    private String customerCode;

    private String salesChannelCode;
    private Long branchId;

    @NotBlank(message = "Thiếu trạng thái đơn hàng")
    private String status;

    private String note;

    // --- Vận chuyển ---
    private String shippingType;
    private Long shipFromBranchId;
    private String shippingPartner;
    private String trackingCode;
    private String shippingAddress;
    private LocalDate expectedDeliveryDate; // Chú ý kiểu dữ liệu ngày tháng
    private BigDecimal shippingFee;
    private BigDecimal codAmount;
    private Double shipWeight;
    private String referenceCode;
    // --- Thanh toán ---
    @NotBlank(message = "Thiếu trạng thái thanh toán")
    private String paymentStatus;
    private String paymentMethod;
    private BigDecimal amountPaid;

    // --- Hóa đơn (Có thể null nếu khách không lấy VAT) ---
    private String invoiceTaxCode;
    private String invoiceCompanyName;
    private String invoiceCompanyAddress;

    // --- Tổng tiền ---
    private BigDecimal discountAmount;

    @NotNull(message = "Tổng tiền không được để trống")
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    // --- Chi tiết sản phẩm ---
    @NotEmpty(message = "Giỏ hàng không được để trống")
    private List<OrderDetailRequest> details;
}