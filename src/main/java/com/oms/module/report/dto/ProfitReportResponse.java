package com.oms.module.report.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProfitReportResponse {
    private Integer month;
    private Integer year;

    private BigDecimal salesRevenue;      // Doanh thu bán hàng (Từ Orders)
    private BigDecimal otherIncome;       // Thu nhập khác (Từ Phiếu Thu)
    private BigDecimal totalRevenue;      // Tổng doanh thu (salesRevenue + otherIncome)

    private BigDecimal costOfGoodsSold;   // Giá vốn hàng bán (tính theo giá vốn thực tế)
    private BigDecimal grossProfit;       // Lãi gộp (totalRevenue - costOfGoodsSold)

    private BigDecimal operatingExpenses; // Chi phí vận hành (Từ Phiếu Chi)
    private BigDecimal netProfit;         // Lợi nhuận ròng (grossProfit - operatingExpenses)
}