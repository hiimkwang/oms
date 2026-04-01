package com.oms.module.dashboard.controller;

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

    // FIX LỖI 1: Bắt cả đường dẫn "/" (Trang chủ) và "/ui/dashboard"
    @GetMapping({"/", "/ui/dashboard", "/dashboard"})
    public String dashboard(
            Model model,
            @RequestParam(required = false) String preset,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        LocalDateTime now = LocalDateTime.now();

        // FIX LỖI 1: Gán MẶC ĐỊNH là "Tháng này" nếu mới truy cập lần đầu (chưa lọc)
        if (preset == null && start == null && end == null) {
            preset = "thisMonth";
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

        // ==========================================
        // 2. GỌI CÁC CHỈ SỐ TÀI CHÍNH & BÁN HÀNG
        // (CHUẨN CÔNG THỨC KẾ TOÁN)
        // ==========================================

        // A. TỔNG DOANH THU (Total Revenue)
        BigDecimal totalRevenue = orderRepo.sumNetRevenue(start, end);
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        // B. GIÁ VỐN HÀNG BÁN (COGS)
        BigDecimal cogs = orderRepo.sumTotalCOGS(start, end);
        if (cogs == null) cogs = BigDecimal.ZERO;

        // C. LỢI NHUẬN GỘP (Gross Profit) = Doanh thu - Giá vốn
        BigDecimal grossProfit = totalRevenue.subtract(cogs);

        // D. CHI PHÍ BÁN HÀNG & QUẢN LÝ (Operating Expenses)
        // Nếu anh có làm module Sổ Quỹ (Thu/Chi), anh sẽ dùng Repository của Sổ Quỹ để SUM các khoản chi trong kỳ.
        // Tạm thời để 0, anh có thể móc data thật vào sau.
        BigDecimal operatingExpenses = BigDecimal.ZERO;
        // VD: operatingExpenses = cashbookRepo.sumTotalExpenses(start, end);

        // E. LỢI NHUẬN TRƯỚC THUẾ
        BigDecimal profitBeforeTax = grossProfit.subtract(operatingExpenses);

        // F. THUẾ TNDN (Tạm tính 20% nếu có lãi, theo đúng ví dụ của anh)
        BigDecimal tax = BigDecimal.ZERO;
        if (profitBeforeTax.compareTo(BigDecimal.ZERO) > 0) {
            // Uncomment dòng dưới nếu anh muốn trừ luôn 20% thuế tự động
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
        // FIX LỖI 2: CHUẨN TÊN TRẠNG THÁI TRONG DB CỦA ANH
        Long unpaidOrders = orderRepo.countUnpaidOrders(start, end);
        Long pendingOrders = orderRepo.countOrdersByStatus("Khởi tạo", start, end);
        Long shippingOrders = orderRepo.countOrdersByStatus("Đang giao hàng", start, end); // <- Sửa ở đây
        Long canceledOrders = orderRepo.countOrdersByStatus("Đã hủy", start, end);         // <- Sửa ở đây

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

        // 3. FIX LỖI 4: VẼ BIỂU ĐỒ (Dữ liệu 6 tháng gần nhất)
        List<String> chartLabels = new ArrayList<>();
        List<BigDecimal> chartRevenue = new ArrayList<>();
        List<BigDecimal> chartProfit = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            YearMonth targetMonth = YearMonth.from(now.minusMonths(i));
            LocalDateTime mStart = targetMonth.atDay(1).atTime(LocalTime.MIN);
            LocalDateTime mEnd = targetMonth.atEndOfMonth().atTime(LocalTime.MAX);

            BigDecimal mRev = orderRepo.sumNetRevenue(mStart, mEnd);
            if (mRev == null) mRev = BigDecimal.ZERO;

            BigDecimal mCogs = orderRepo.sumTotalCOGS(mStart, mEnd);
            if (mCogs == null) mCogs = BigDecimal.ZERO;

            BigDecimal mExp = BigDecimal.ZERO; // Sửa thành cashRepo.sumOperatingExpenses(mStart, mEnd) nếu anh có sổ quỹ

            chartLabels.add("T" + targetMonth.getMonthValue());
            chartRevenue.add(mRev);
            chartProfit.add(mRev.subtract(mCogs).subtract(mExp));
        }

        // Bắt buộc đẩy mảng rỗng nếu không có dữ liệu để JS không bị lỗi undefined
        model.addAttribute("chartLabels", chartLabels);
        model.addAttribute("chartRevenue", chartRevenue);
        model.addAttribute("chartProfit", chartProfit);

        // Biểu đồ tròn: Kênh bán hàng
        List<Object[]> channelStats = orderRepo.countOrdersByChannel(start, end);
        List<String> channelLabels = new ArrayList<>();
        List<Long> channelData = new ArrayList<>();

        if (channelStats != null && !channelStats.isEmpty()) {
            for (Object[] stat : channelStats) {
                channelLabels.add(stat[0] != null ? stat[0].toString() : "Khác");
                channelData.add(stat[1] != null ? ((Number) stat[1]).longValue() : 0L);
            }
        } else {
            // Dữ liệu mẫu nếu chưa có đơn nào để vẽ vòng tròn
            channelLabels.add("Chưa có đơn");
            channelData.add(1L);
        }
        model.addAttribute("channelLabels", channelLabels);
        model.addAttribute("channelData", channelData);

        // Danh sách đơn hàng gần đây
        List<Order> recentOrders = orderRepo.findTop5ByOrderByCreatedAtDesc();
        model.addAttribute("recentOrders", recentOrders != null ? recentOrders : new ArrayList<>());

        return "dashboard"; // Trả về file html của anh
    }
}