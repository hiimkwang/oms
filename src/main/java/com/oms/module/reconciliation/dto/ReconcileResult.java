package com.oms.module.reconciliation.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Kết quả tổng hợp của một lần đối soát.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReconcileResult {
    private boolean success;
    private String message;

    private String amountColumnName; // Tên cột số tiền đã dùng để đối chiếu (minh bạch)
    private int excludedCancelled;   // Số đơn HỦY trên sàn đã bị loại khỏi đối soát

    private int totalFileRows;     // Số dòng đọc được từ file sàn
    private int matchedCount;       // Số đơn khớp
    private int missingOmsCount;    // Số đơn sàn chưa có trên OMS (cần tạo bù)
    private int missingFileCount;   // Số đơn OMS không thấy trong file (chưa đối soát)

    private BigDecimal totalSanAmount; // Tổng doanh thu sàn (các dòng khớp + thiếu OMS)
    private BigDecimal totalFee;        // Tổng phí sàn
    private BigDecimal totalNetReceived;// Tổng thực nhận ước tính (doanh thu - phí)
    private BigDecimal totalOmsAmount; // Tổng tiền OMS (các dòng khớp)
    private BigDecimal totalDiff;      // Tổng chênh lệch doanh thu các dòng khớp

    private List<ReconcileRow> rows;
}
