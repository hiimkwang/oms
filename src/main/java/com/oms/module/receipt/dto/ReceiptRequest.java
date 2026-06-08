package com.oms.module.receipt.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReceiptRequest {
    @NotBlank(message = "Vui lòng chọn nhà cung cấp")
    private String supplierCode;
    private String branchName;
    private Long branchId;
    private String note;

    private BigDecimal itemsAmount;  // Tổng tiền hàng
    private BigDecimal discount;     // Giảm giá
    private BigDecimal shippingFee;  // Phí nhập hàng
    private BigDecimal totalAmount;  // Tổng cần trả
    private BigDecimal amountPaid;   // Đã trả

    private String paymentStatus;
    private String paymentMethod;

    private Boolean isImportStock;
    private String referenceCode; // Số hóa đơn NCC (Nếu cần)
    private Long assigneeId;      // Nhân viên phụ trách (Nếu cần)

    @NotEmpty(message = "Phiếu nhập phải có ít nhất 1 sản phẩm")
    @Valid
    private List<ItemRequest> items;
    private LocalDateTime createdAt;
    @Data
    public static class ItemRequest {
        @NotBlank(message = "SKU sản phẩm không được trống")
        private String sku;
        @Min(value = 1, message = "Số lượng nhập phải lớn hơn 0")
        private int quantity;
        @NotNull(message = "Giá nhập không được trống")
        @DecimalMin(value = "0.0", message = "Giá nhập không được âm")
        private BigDecimal importPrice;
        private Integer warrantyMonths;
    }
}