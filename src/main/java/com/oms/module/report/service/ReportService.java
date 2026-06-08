package com.oms.module.report.service;

import com.oms.module.cashbook.repository.CashTransactionRepository;
import com.oms.module.order.repository.OrderRepository;
import com.oms.module.report.dto.ProfitReportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final OrderRepository orderRepository;
    private final CashTransactionRepository cashTransactionRepository;

    private BigDecimal nz(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    public ProfitReportResponse getMonthlyProfitReport(int month, int year) {

        // 1. Doanh thu bán hàng từ Đơn hàng (BigDecimal, đã loại đơn Hủy/Khởi tạo)
        BigDecimal salesRevenue = nz(orderRepository.sumTotalRevenueBigDecimal(month, year));

        // 2. Thu nhập khác từ Phiếu Thu
        BigDecimal otherIncome = nz(cashTransactionRepository.sumOtherIncomeByMonthAndYearBD(month, year));

        // => Tổng doanh thu
        BigDecimal totalRevenue = salesRevenue.add(otherIncome);

        // 3. Giá vốn hàng bán (COGS) - lấy theo GIÁ VỐN THỰC TẾ trong OrderDetail (không ước lượng 75%)
        BigDecimal costOfGoodsSold = nz(orderRepository.sumCostOfGoodsSoldByMonthAndYearBD(month, year));

        // => Lãi gộp = Tổng doanh thu - Giá vốn
        BigDecimal grossProfit = totalRevenue.subtract(costOfGoodsSold);

        // 4. Chi phí vận hành từ Phiếu Chi
        BigDecimal operatingExpenses = nz(cashTransactionRepository.sumOperatingExpensesByMonthAndYearBD(month, year));

        // => Lợi nhuận ròng = Lãi gộp - Chi phí vận hành
        BigDecimal netProfit = grossProfit.subtract(operatingExpenses);

        return ProfitReportResponse.builder().month(month).year(year).salesRevenue(salesRevenue).otherIncome(otherIncome).totalRevenue(totalRevenue).costOfGoodsSold(costOfGoodsSold).grossProfit(grossProfit).operatingExpenses(operatingExpenses).netProfit(netProfit).build();
    }
}