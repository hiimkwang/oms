package com.oms.module.report.service;

import com.oms.module.cashbook.repository.CashTransactionRepository;
import com.oms.module.order.repository.OrderRepository;
import com.oms.module.report.dto.ProfitReportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final OrderRepository orderRepository;
    private final CashTransactionRepository cashTransactionRepository;

    public ProfitReportResponse getMonthlyProfitReport(int month, int year) {

        // 1. Lấy Doanh thu bán hàng từ Đơn hàng
        Double salesRevenue = orderRepository.sumTotalRevenueByMonthAndYear(month, year);

        // 2. Lấy Thu nhập khác từ Phiếu Thu
        Double otherIncome = cashTransactionRepository.sumOtherIncomeByMonthAndYear(month, year);

        // => Tổng doanh thu
        Double totalRevenue = salesRevenue + otherIncome;

        // 3. Tính Giá vốn hàng bán (COGS)
        // Lưu ý: Để tính chuẩn xác 100% giống file DATA_XUAT.csv, bạn cần lưu trường `costPrice` vào OrderDetail khi tạo đơn.
        // Ở đây tôi đang giả lập giá vốn chiếm 75% doanh thu bán hàng làm ví dụ (Biên lợi nhuận 25%).
        Double costOfGoodsSold = salesRevenue * 0.75;

        // => Lãi gộp
        Double grossProfit = totalRevenue - costOfGoodsSold;

        // 4. Lấy Chi phí vận hành từ Phiếu Chi (Tiền server, quảng cáo, v.v.)
        Double operatingExpenses = cashTransactionRepository.sumOperatingExpensesByMonthAndYear(month, year);

        // => Lợi Nhuận Ròng
        Double netProfit = grossProfit - operatingExpenses;

        return ProfitReportResponse.builder()
                .month(month)
                .year(year)
                .salesRevenue(salesRevenue)
                .otherIncome(otherIncome)
                .totalRevenue(totalRevenue)
                .costOfGoodsSold(costOfGoodsSold)
                .grossProfit(grossProfit)
                .operatingExpenses(operatingExpenses)
                .netProfit(netProfit)
                .build();
    }
}