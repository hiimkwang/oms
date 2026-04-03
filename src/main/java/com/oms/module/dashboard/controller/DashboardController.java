package com.oms.module.dashboard.controller;

import com.oms.module.cashbook.entity.CashTransaction;
import com.oms.module.cashbook.repository.CashTransactionRepository;
import com.oms.module.order.entity.Order;
import com.oms.module.order.repository.OrderRepository;
import com.oms.module.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final OrderRepository orderRepo;
    private final OrderService orderService;
    private final CashTransactionRepository cashRepo; // Bảng lưu Phiếu thu/chi

    @GetMapping({"/", "/ui/dashboard", "/dashboard"})
    public String dashboard(
            Model model,
            @RequestParam(required = false) String preset,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        LocalDateTime now = LocalDateTime.now();

        if (preset == null && start == null && end == null) {
            preset = "thisWeek";
        }

        // 1. LOGIC XỬ LÝ THỜI GIAN
        if ("custom".equals(preset)) {
            if (start == null) start = now.with(LocalTime.MIN);
            if (end == null) end = now.with(LocalTime.MAX);
        } else {
            switch (preset) {
                case "thisMonth":
                    start = now.withDayOfMonth(1).with(LocalTime.MIN);
                    end = now.with(TemporalAdjusters.lastDayOfMonth()).with(LocalTime.MAX);
                    break;
                case "thisQuarter":
                    int currentMonth = now.getMonthValue();
                    int firstMonthOfQuarter = ((currentMonth - 1) / 3) * 3 + 1;
                    start = now.withMonth(firstMonthOfQuarter).withDayOfMonth(1).with(LocalTime.MIN);
                    end = start.plusMonths(2).with(TemporalAdjusters.lastDayOfMonth()).with(LocalTime.MAX);
                    break;
                case "thisYear":
                    start = now.withDayOfYear(1).with(LocalTime.MIN);
                    end = now.with(TemporalAdjusters.lastDayOfYear()).with(LocalTime.MAX);
                    break;
                case "thisWeek":
                default:
                    start = now.with(DayOfWeek.MONDAY).with(LocalTime.MIN);
                    end = now.with(DayOfWeek.SUNDAY).with(LocalTime.MAX);
                    break;
            }
        }

        model.addAttribute("preset", preset);
        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);

        // ==========================================
        // 2. GỌI CÁC CHỈ SỐ TÀI CHÍNH & BÁN HÀNG
        // ==========================================

        // A. TỔNG DOANH THU (Total Revenue)
        BigDecimal totalRevenue = orderRepo.sumNetRevenue(start, end);
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        // B. GIÁ VỐN HÀNG BÁN (COGS)
        BigDecimal cogs = orderRepo.sumTotalCOGS(start, end);
        if (cogs == null) cogs = BigDecimal.ZERO;

        // C. LỢI NHUẬN GỘP (Gross Profit) = Doanh thu - Giá vốn
        BigDecimal grossProfit = totalRevenue.subtract(cogs);

        // D. CHI PHÍ BÁN HÀNG & QUẢN LÝ (Operating Expenses từ Sổ Quỹ)
        BigDecimal operatingExpenses = cashRepo.sumOperatingExpensesBetweenDates(start, end);
        if (operatingExpenses == null) operatingExpenses = BigDecimal.ZERO;

        BigDecimal otherIncome = cashRepo.sumOtherIncomeBetweenDates(start, end);
        if (otherIncome == null) otherIncome = BigDecimal.ZERO;

        // E. LỢI NHUẬN TRƯỚC THUẾ
        BigDecimal profitBeforeTax = grossProfit.add(otherIncome).subtract(operatingExpenses);

        // F. THUẾ TNDN (Tạm tính 20% nếu có lãi, theo đúng ví dụ của anh)
        BigDecimal tax = BigDecimal.ZERO;
        if (profitBeforeTax.compareTo(BigDecimal.ZERO) > 0) {
            // tax = profitBeforeTax.multiply(new BigDecimal("0.20"));
        }

        // G. LỢI NHUẬN RÒNG CUỐI CÙNG (Net Profit)
        BigDecimal netProfit = profitBeforeTax.subtract(tax);

        // ------------------------------------------
        // CÁC CHỈ SỐ VẬN HÀNH KHÁC
        // ------------------------------------------
        Long totalOrders = orderRepo.countTotalOrders(start, end);
        Long totalItemsSold = orderRepo.sumTotalItemsSold(start, end);

        BigDecimal aov = BigDecimal.ZERO;
        if (totalOrders != null && totalOrders > 0) {
            aov = totalRevenue.divide(BigDecimal.valueOf(totalOrders), 0, RoundingMode.HALF_UP);
        }

        Long unpaidOrders = orderRepo.countUnpaidOrders(start, end);
        Long pendingOrders = orderRepo.countOrdersByStatus("Khởi tạo", start, end);
        Long shippingOrders = orderRepo.countOrdersByStatus("Đang giao hàng", start, end);
        Long canceledOrders = orderRepo.countOrdersByStatus("Đã hủy", start, end);

        // Đẩy 8 thẻ bài ra View
        model.addAttribute("netRevenue", totalRevenue);
        model.addAttribute("netProfit", netProfit);
        model.addAttribute("aov", aov);
        model.addAttribute("totalItemsSold", totalItemsSold != null ? totalItemsSold : 0);
        model.addAttribute("totalOrders", totalOrders != null ? totalOrders : 0);
        model.addAttribute("unpaidOrders", unpaidOrders != null ? unpaidOrders : 0);
        model.addAttribute("pendingOrders", pendingOrders != null ? pendingOrders : 0);
        model.addAttribute("shippingOrders", shippingOrders != null ? shippingOrders : 0);
        model.addAttribute("canceledOrders", canceledOrders != null ? canceledOrders : 0);

        // ==========================================
        // 3. VẼ BIỂU ĐỒ (BÁM SÁT VÀO BỘ LỌC THỜI GIAN)
        // ==========================================
        List<String> chartLabels = new ArrayList<>();
        List<BigDecimal> chartRevenue = new ArrayList<>();
        List<BigDecimal> chartExpenses = new ArrayList<>(); // Cột mới cho Chi phí
        List<BigDecimal> chartProfit = new ArrayList<>();

        long daysBetween = java.time.Duration.between(start, end).toDays();

        // NẾU LỌC DƯỚI 35 NGÀY -> VẼ THEO TỪNG NGÀY
        if (daysBetween <= 35) {
            for (int i = 0; i <= daysBetween; i++) {
                LocalDateTime dayStart = start.plusDays(i).with(LocalTime.MIN);
                LocalDateTime dayEnd = dayStart.with(LocalTime.MAX);

                BigDecimal dRev = orderRepo.sumNetRevenue(dayStart, dayEnd);
                if (dRev == null) dRev = BigDecimal.ZERO;

                BigDecimal dCogs = orderRepo.sumTotalCOGS(dayStart, dayEnd);
                if (dCogs == null) dCogs = BigDecimal.ZERO;

                // Sửa đoạn tính chi phí và lợi nhuận ở vòng lặp < 35 ngày
                BigDecimal dExp = cashRepo.sumOperatingExpensesBetweenDates(dayStart, dayEnd);
                if (dExp == null) dExp = BigDecimal.ZERO;

                BigDecimal dOther = cashRepo.sumOtherIncomeBetweenDates(dayStart, dayEnd);
                if (dOther == null) dOther = BigDecimal.ZERO;

                chartLabels.add(dayStart.getDayOfMonth() + "/" + dayStart.getMonthValue());
                chartRevenue.add(dRev);
                chartExpenses.add(dExp);
                chartProfit.add(dRev.subtract(dCogs).add(dOther).subtract(dExp)); // Cập nhật công thức Chart
            }
        }
        // NẾU LỌC DÀI HƠN -> VẼ THEO TỪNG THÁNG
        else {
            YearMonth startMonth = YearMonth.from(start);
            YearMonth endMonth = YearMonth.from(end);

            while (!startMonth.isAfter(endMonth)) {
                LocalDateTime mStart = startMonth.atDay(1).atTime(LocalTime.MIN);
                LocalDateTime mEnd = startMonth.atEndOfMonth().atTime(LocalTime.MAX);

                BigDecimal mRev = orderRepo.sumNetRevenue(mStart, mEnd);
                if (mRev == null) mRev = BigDecimal.ZERO;

                BigDecimal mCogs = orderRepo.sumTotalCOGS(mStart, mEnd);
                if (mCogs == null) mCogs = BigDecimal.ZERO;

                // Sửa đoạn tính chi phí và lợi nhuận ở vòng lặp Tháng
                BigDecimal mExp = cashRepo.sumOperatingExpensesBetweenDates(mStart, mEnd);
                if (mExp == null) mExp = BigDecimal.ZERO;

                BigDecimal mOther = cashRepo.sumOtherIncomeBetweenDates(mStart, mEnd);
                if (mOther == null) mOther = BigDecimal.ZERO;

                chartLabels.add("T" + startMonth.getMonthValue());
                chartRevenue.add(mRev);
                chartExpenses.add(mExp);
                chartProfit.add(mRev.subtract(mCogs).add(mOther).subtract(mExp)); // Cập nhật công thức Chart

                startMonth = startMonth.plusMonths(1);
            }
        }

        model.addAttribute("chartLabels", chartLabels);
        model.addAttribute("chartRevenue", chartRevenue);
        model.addAttribute("chartExpenses", chartExpenses); // Ném mảng Chi phí ra cho JS
        model.addAttribute("chartProfit", chartProfit);

        // ==========================================
        // 4. BẢNG TOP SẢN PHẨM BÁN CHẠY
        // ==========================================
        // Lấy top 5 sản phẩm (Cần import org.springframework.data.domain.PageRequest)
        List<Object[]> topProducts = orderRepo.findDashboardTopProducts(start, end, org.springframework.data.domain.PageRequest.of(0, 5));
        model.addAttribute("topProducts", topProducts != null ? topProducts : new ArrayList<>());

        // ==========================================
        // 5. BIỂU ĐỒ TRÒN KÊNH BÁN & ĐƠN HÀNG GẦN ĐÂY
        // ==========================================
        List<Object[]> channelStats = orderRepo.countOrdersByChannel(start, end);
        List<String> channelLabels = new ArrayList<>();
        List<Long> channelData = new ArrayList<>();

        if (channelStats != null && !channelStats.isEmpty()) {
            for (Object[] stat : channelStats) {
                channelLabels.add(stat[0] != null ? stat[0].toString() : "Khác");
                channelData.add(stat[1] != null ? ((Number) stat[1]).longValue() : 0L);
            }
        } else {
            channelLabels.add("Chưa có đơn");
            channelData.add(1L);
        }
        model.addAttribute("channelLabels", channelLabels);
        model.addAttribute("channelData", channelData);

        List<Order> recentOrders = orderRepo.findTop5ByOrderByCreatedAtDesc();
        model.addAttribute("recentOrders", recentOrders != null ? recentOrders : new ArrayList<>());

        return "dashboard";
    }
}