package com.oms.module.report.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfitReportResponse {
    private Integer month;
    private Integer year;

    private Double salesRevenue;      // Doanh thu bán hàng (Từ Orders)
    private Double otherIncome;       // Thu nhập khác (Từ Phiếu Thu)
    private Double totalRevenue;      // Tổng doanh thu (salesRevenue + otherIncome)

    private Double costOfGoodsSold;   // Giá vốn hàng bán (Tạm tính)
    private Double grossProfit;       // Lãi gộp (totalRevenue - costOfGoodsSold)

    private Double operatingExpenses; // Chi phí vận hành (Từ Phiếu Chi)
    private Double netProfit;         // Lợi nhuận ròng (grossProfit - operatingExpenses)
}