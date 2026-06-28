package com.oms.module.cashbook.dto;

import com.oms.module.cashbook.entity.CashTransaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Báo cáo Thu/Chi GỘP THEO LOẠI (lý do) trong một khoảng thời gian.
 * Cho phép xem nhanh: thu/chi vào từng nhóm bao nhiêu (vd: Chi phí vận hành, Trả nợ NCC, Thu tiền bán hàng...).
 * Nếu có từ khóa, lọc thêm theo nội dung (mô tả/đối tượng/lý do) để bóc tách khoản cụ thể (vd "Quảng cáo").
 */
@Data
@Builder
public class CashFlowReport {
    private List<Line> receiptLines;   // các nhóm phiếu THU
    private List<Line> paymentLines;   // các nhóm phiếu CHI
    private BigDecimal totalIn;
    private BigDecimal totalOut;
    private BigDecimal net;             // totalIn - totalOut

    // Khi tìm theo từ khóa: danh sách phiếu khớp + tổng tiền của chúng (tách thu/chi)
    private String keyword;
    private List<CashTransaction> matched;
    private BigDecimal matchedIn;
    private BigDecimal matchedOut;

    @Data
    @AllArgsConstructor
    public static class Line {
        private String reason;       // tên loại (lý do)
        private BigDecimal amount;   // tổng tiền của loại này
        private long count;          // số phiếu
        private double percent;      // tỉ trọng so với tổng thu (hoặc tổng chi)
    }
}
