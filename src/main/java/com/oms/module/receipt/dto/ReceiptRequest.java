package com.oms.module.receipt.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ReceiptRequest {
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

    private List<ItemRequest> items;

    @Data
    public static class ItemRequest {
        private String sku;
        private int quantity;
        private BigDecimal importPrice;
        private Integer warrantyMonths;
    }
}