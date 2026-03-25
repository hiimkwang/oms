package com.oms.module.receipt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SupplierStatsResponse {
    private long totalOrders;          // Tổng số đơn nhập đã tạo
    private BigDecimal totalAmount;    // Tổng giá trị đơn nhập
    private BigDecimal totalDebt;      // Tổng nợ cần trả (UNPAID)
    private long returnOrders;         // Đơn trả hàng (nếu ông có làm module trả)
    private List<ReceiptSummary> history; // Danh sách đơn hàng để hiện ở bảng bên dưới

    @Data
    @Builder
    public static class ReceiptSummary {
        private String code;
        private LocalDateTime createdAt;
        private String status;
        private String paymentStatus;
        private BigDecimal totalAmount;
    }
}