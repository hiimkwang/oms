package com.oms.module.report.service;

import com.oms.module.cashbook.repository.CashTransactionRepository;
import com.oms.module.inventory.repository.InventoryRepository;
import com.oms.module.order.repository.OrderRepository;
import com.oms.module.report.dto.InventoryMovementRow;
import com.oms.module.report.dto.ProfitReportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final OrderRepository orderRepository;
    private final CashTransactionRepository cashTransactionRepository;
    private final InventoryRepository inventoryRepository;

    private BigDecimal nz(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    public ProfitReportResponse getMonthlyProfitReport(int month, int year) {

        // 1. Doanh thu bán hàng từ Đơn hàng (BigDecimal, đã loại đơn Hủy/Khởi tạo)
        BigDecimal salesRevenue = nz(orderRepository.sumTotalRevenueBigDecimal(month, year));

        // 2. Thu nhập khác từ Phiếu Thu
        BigDecimal otherIncome = nz(cashTransactionRepository.sumOtherIncomeByMonthAndYearBD(month, year));

        // => Tổng doanh thu (dùng để hiển thị)
        BigDecimal totalRevenue = salesRevenue.add(otherIncome);

        // 3. Giá vốn hàng bán (COGS) - lấy theo GIÁ VỐN THỰC TẾ trong OrderDetail (không ước lượng 75%)
        BigDecimal costOfGoodsSold = nz(orderRepository.sumCostOfGoodsSoldByMonthAndYearBD(month, year));

        // => Lãi gộp = Doanh thu BÁN HÀNG - Giá vốn (KHÔNG cộng thu nhập khác vào lãi gộp)
        BigDecimal grossProfit = salesRevenue.subtract(costOfGoodsSold);

        // 4. Chi phí vận hành từ Phiếu Chi
        BigDecimal operatingExpenses = nz(cashTransactionRepository.sumOperatingExpensesByMonthAndYearBD(month, year));

        // => Lợi nhuận ròng = Lãi gộp + Thu nhập khác - Chi phí vận hành
        BigDecimal netProfit = grossProfit.add(otherIncome).subtract(operatingExpenses);

        return ProfitReportResponse.builder().month(month).year(year).salesRevenue(salesRevenue).otherIncome(otherIncome).totalRevenue(totalRevenue).costOfGoodsSold(costOfGoodsSold).grossProfit(grossProfit).operatingExpenses(operatingExpenses).netProfit(netProfit).build();
    }

    /**
     * Báo cáo Bán chạy / Tồn đọng theo từng SKU trong khoảng thời gian.
     * Ghép SL bán trong kỳ với tồn kho hiện tại để xác định tốc độ xoay vòng và vốn đang chôn.
     */
    public List<InventoryMovementRow> getInventoryMovement(LocalDateTime start, LocalDateTime end) {
        // 1. Bản đồ SL bán & doanh thu theo SKU trong kỳ
        Map<String, long[]> soldMap = new HashMap<>();      // sku -> [qty]
        Map<String, BigDecimal> revenueMap = new HashMap<>();
        for (Object[] row : orderRepository.findSkuSalesBetween(start, end)) {
            String sku = row[0] != null ? row[0].toString() : null;
            if (sku == null) continue;
            long qty = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            BigDecimal rev = row[3] != null ? (BigDecimal) row[3] : BigDecimal.ZERO;
            soldMap.put(sku, new long[]{qty});
            revenueMap.put(sku, rev);
        }

        // 2. Số ngày trong kỳ (tối thiểu 1) để tính tốc độ bán trung bình/ngày
        long daysInPeriod = ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate()) + 1;
        if (daysInPeriod < 1) daysInPeriod = 1;

        // 3. Duyệt toàn bộ biến thể có tồn để phân loại
        List<InventoryMovementRow> result = new ArrayList<>();
        for (Object[] v : inventoryRepository.findAllStockedVariants()) {
            String sku = v[0] != null ? v[0].toString() : "";
            String productName = v[1] != null ? v[1].toString() : sku;
            String variantName = v[2] != null ? v[2].toString() : "";
            int stock = v[3] != null ? ((Number) v[3]).intValue() : 0;
            BigDecimal cost = v[4] != null ? (BigDecimal) v[4] : BigDecimal.ZERO;
            String imageUrl = v[5] != null ? v[5].toString() : null;

            long soldQty = soldMap.containsKey(sku) ? soldMap.get(sku)[0] : 0L;
            BigDecimal revenue = revenueMap.getOrDefault(sku, BigDecimal.ZERO);
            BigDecimal costValue = cost.multiply(BigDecimal.valueOf(Math.max(stock, 0)));

            Integer daysOfStock = null;
            double avgDaily = (double) soldQty / daysInPeriod;
            if (avgDaily > 0 && stock > 0) {
                daysOfStock = (int) Math.ceil(stock / avgDaily);
            }

            String category;
            String label;
            if (stock <= 0) {
                category = "SOLD_OUT";
                label = "Hết hàng";
            } else if (soldQty == 0) {
                category = "DEAD";
                label = "Tồn đọng";
            } else if (daysOfStock != null && daysOfStock <= 15) {
                category = "FAST";
                label = "Bán chạy";
            } else if (daysOfStock != null && daysOfStock >= 60) {
                category = "SLOW";
                label = "Bán chậm";
            } else {
                category = "NORMAL";
                label = "Bình thường";
            }

            result.add(InventoryMovementRow.builder()
                    .sku(sku).productName(productName).variantName(variantName).imageUrl(imageUrl)
                    .stock(stock).soldQty(soldQty).revenue(revenue).costValue(costValue)
                    .daysOfStock(daysOfStock).category(category).categoryLabel(label)
                    .build());
        }

        // Sắp xếp: bán chạy (SL bán cao) lên đầu
        result.sort(Comparator.comparingLong(InventoryMovementRow::getSoldQty).reversed());
        return result;
    }
}