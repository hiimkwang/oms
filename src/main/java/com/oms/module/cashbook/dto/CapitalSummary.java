package com.oms.module.cashbook.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * Tổng quan tình hình VỐN của cửa hàng.
 * Tái sử dụng dữ liệu Sổ quỹ (phiếu thu/chi) + giá trị tồn kho theo giá vốn.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CapitalSummary {
    // Tổng vốn đã góp vào (các phiếu THU có lý do "Nhận vốn góp")
    private BigDecimal totalContributed;

    // Tổng vốn/lợi nhuận đã rút ra (các phiếu CHI có lý do "Rút vốn")
    private BigDecimal totalWithdrawn;

    // Vốn ròng đã bỏ vào = đã góp - đã rút
    private BigDecimal netCapital;

    // Giá trị hàng tồn kho hiện tại tính theo GIÁ VỐN (vốn đang nằm trong hàng hóa)
    private BigDecimal inventoryValue;

    // Tiền mặt hiện có (toàn thời gian)
    private BigDecimal cashBalance;

    // Tiền ngân hàng hiện có (toàn thời gian)
    private BigDecimal bankBalance;

    // Tổng tiền quỹ hiện có = tiền mặt + ngân hàng
    private BigDecimal fundBalance;

    // Tổng tiền đã THU từ bán hàng (các phiếu thu lý do "Thu tiền bán hàng")
    private BigDecimal salesReceived;

    // Tổng tài sản ước tính hiện có = tiền quỹ + giá trị tồn kho
    private BigDecimal currentAssets;

    // Công nợ KHÁCH còn nợ mình (phải thu)
    private BigDecimal receivables;

    // Công nợ mình còn nợ NCC (phải trả)
    private BigDecimal payables;

    // Vốn thực = tiền quỹ + tồn kho + phải thu - phải trả
    private BigDecimal netWorth;

    // Chênh lệch so với vốn ròng đã bỏ vào ( = netWorth - netCapital )
    // Dương: tài sản thực đã lớn hơn vốn bỏ ra (làm ăn có tích lũy); Âm: vốn đang bị hao hụt.
    private BigDecimal growth;
}
