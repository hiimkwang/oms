package com.oms.module.report.controller;

import com.oms.module.cashbook.entity.CashTransaction;
import com.oms.module.cashbook.repository.CashTransactionRepository;
import com.oms.module.inventory.repository.InventoryRepository;
import com.oms.module.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

@Controller
@RequiredArgsConstructor
public class ReportController {

    private final OrderRepository orderRepo;
    private final InventoryRepository inventoryRepo;
    private final CashTransactionRepository cashTransactionRepository;

    @GetMapping("/ui/reports")
    public String reportOverview(
            Model model,
            @RequestParam(required = false, defaultValue = "overview") String tab,
            @RequestParam(required = false, defaultValue = "30days") String preset,
            @RequestParam(required = false) String channel, // <--- THÊM BIẾN NÀY VÀO ĐÂY
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = start;
        LocalDateTime endTime = end;

        // XỬ LÝ PRESET THỜI GIAN (Giữ nguyên không đổi)
        if (!"custom".equals(preset)) {
            endTime = now.with(LocalTime.MAX);
            switch (preset) {
                case "today":
                    startTime = now.with(LocalTime.MIN);
                    break;
                case "yesterday":
                    startTime = now.minusDays(1).with(LocalTime.MIN);
                    endTime = now.minusDays(1).with(LocalTime.MAX);
                    break;
                case "7days":
                    startTime = now.minusDays(7).with(LocalTime.MIN);
                    break;
                case "30days":
                    startTime = now.minusDays(30).with(LocalTime.MIN);
                    break;
                case "thisWeek":
                    startTime = now.with(java.time.DayOfWeek.MONDAY).with(LocalTime.MIN);
                    break;
                case "lastWeek":
                    startTime = now.minusWeeks(1).with(java.time.DayOfWeek.MONDAY).with(LocalTime.MIN);
                    endTime = startTime.plusDays(6).with(LocalTime.MAX);
                    break;
                case "thisMonth":
                    startTime = now.withDayOfMonth(1).with(LocalTime.MIN);
                    break;
                case "lastMonth":
                    startTime = now.minusMonths(1).withDayOfMonth(1).with(LocalTime.MIN);
                    endTime = startTime.with(TemporalAdjusters.lastDayOfMonth()).with(LocalTime.MAX);
                    break;
                case "thisYear":
                    startTime = now.withDayOfYear(1).with(LocalTime.MIN);
                    break;
                case "lastYear":
                    startTime = now.minusYears(1).withDayOfYear(1).with(LocalTime.MIN);
                    endTime = startTime.with(TemporalAdjusters.lastDayOfYear()).with(LocalTime.MAX);
                    break;
                default:
                    startTime = now.minusDays(30).with(LocalTime.MIN);
            }
        }

        model.addAttribute("preset", preset);
        model.addAttribute("startDate", startTime);
        model.addAttribute("endDate", endTime);
        model.addAttribute("tab", tab);
        model.addAttribute("selectedChannel", channel); // Ném tên Kênh đang chọn xuống View

        if ("overview".equals(tab)) {
            // ... (Giữ nguyên)
            loadGeneralKPIs(model, startTime, endTime);
            loadTimeCharts(model, startTime, endTime);
            loadTopProducts(model, startTime, endTime);
            loadChannelAndBranchRevenue(model, startTime, endTime);

        } else if ("revenue".equals(tab)) {
            // ... (Giữ nguyên)
            loadGeneralKPIs(model, startTime, endTime);
            loadTimeCharts(model, startTime, endTime);
            loadTopProducts(model, startTime, endTime);
            loadChannelAndBranchRevenue(model, startTime, endTime);
            List<Object[]> profitByDateRaw = orderRepo.findProfitByDate(startTime, endTime);
            List<BigDecimal> marginData = new ArrayList<>();
            List<BigDecimal> revData = (List<BigDecimal>) model.getAttribute("revData");
            if (revData != null && !revData.isEmpty()) {
                for (int i = 0; i < profitByDateRaw.size(); i++) {
                    BigDecimal profit = (BigDecimal) profitByDateRaw.get(i)[1];
                    BigDecimal rev = revData.get(i);
                    marginData.add(rev.compareTo(BigDecimal.ZERO) > 0 ? profit.divide(rev, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")) : BigDecimal.ZERO);
                }
            }
            model.addAttribute("marginData", marginData);
            model.addAttribute("channelProfitData", extractBigDecimalData(orderRepo.findProfitByChannel(startTime, endTime)));
            model.addAttribute("branchProfitData", extractBigDecimalData(orderRepo.findProfitByBranch(startTime, endTime)));

        } else if ("customer".equals(tab)) {
            // ... (Giữ nguyên)
            loadGeneralKPIs(model, startTime, endTime);
            loadTimeCharts(model, startTime, endTime);
            List<Object[]> topCusRaw = orderRepo.findTopCustomers(startTime, endTime, PageRequest.of(0, 5));
            List<String> topCusLabels = new ArrayList<>();
            List<BigDecimal> topCusData = new ArrayList<>();
            for (Object[] obj : topCusRaw) {
                topCusLabels.add(obj[0] != null ? obj[0].toString() : "Khách lẻ");
                topCusData.add(obj[1] != null ? (BigDecimal) obj[1] : BigDecimal.ZERO);
            }
            model.addAttribute("topCusLabels", topCusLabels);
            model.addAttribute("topCusData", topCusData);
            List<Object[]> revByBranchRaw = orderRepo.findRevenueByBranch(startTime, endTime);
            List<String> regionLabels = new ArrayList<>();
            List<BigDecimal> regionData = new ArrayList<>();
            for (Object[] obj : revByBranchRaw) {
                regionLabels.add(obj[0] != null ? obj[0].toString() : "Khác");
                regionData.add(obj[1] != null ? (BigDecimal) obj[1] : BigDecimal.ZERO);
            }
            model.addAttribute("regionLabels", regionLabels);
            model.addAttribute("regionData", regionData);

        } else if ("sales_channel".equals(tab)) {
            // 1. Quét DB lấy toàn bộ kênh bán thực tế
            List<String> allDbChannels = orderRepo.findAllDistinctChannels();

            // Lọc bỏ các giá trị rỗng nếu có rác trong DB
            allDbChannels.removeIf(c -> c == null || c.trim().isEmpty());

            // Nếu DB chưa có đơn nào, gán 1 giá trị mồi để tránh lỗi SQL IN() rỗng
            if (allDbChannels.isEmpty()) {
                allDbChannels.add("CHUA_CO_DATA");
            }
            model.addAttribute("activeChannels", allDbChannels);

            // 2. Xử lý bộ lọc: Chọn kênh cụ thể hoặc lấy tất cả
            List<String> targetChannels = new ArrayList<>();
            if (channel != null && !channel.trim().isEmpty()) {
                targetChannels.add(channel);
            } else {
                targetChannels.addAll(allDbChannels);
            }

            // 3. Nạp dữ liệu
            BigDecimal pRev = orderRepo.sumChannelTabNetRevenue(targetChannels, startTime, endTime);
            BigDecimal pCogs = orderRepo.sumChannelTabCOGS(targetChannels, startTime, endTime);
            model.addAttribute("chanNetRev", pRev != null ? pRev : BigDecimal.ZERO);
            model.addAttribute("chanGrossProfit", (pRev != null ? pRev : BigDecimal.ZERO).subtract(pCogs != null ? pCogs : BigDecimal.ZERO));
            model.addAttribute("chanOrders", orderRepo.countTotalChannelTabOrders(targetChannels, startTime, endTime));
            model.addAttribute("chanCanceled", orderRepo.countChannelTabOrdersByStatus(targetChannels, "Đã hủy", startTime, endTime));
            model.addAttribute("chanReturned", orderRepo.countChannelTabOrdersByStatus(targetChannels, "Trả hàng", startTime, endTime));
            model.addAttribute("chanShipped", orderRepo.countChannelTabOrdersByStatus(targetChannels, "Hoàn thành", startTime, endTime));

            model.addAttribute("chanChannelLabels", extractStringLabels(orderRepo.findChannelTabRevenueByChannel(targetChannels, startTime, endTime)));
            model.addAttribute("chanChannelData", extractBigDecimalData(orderRepo.findChannelTabRevenueByChannel(targetChannels, startTime, endTime)));

            model.addAttribute("chanBranchLabels", extractStringLabels(orderRepo.findChannelTabRevenueByBranch(targetChannels, startTime, endTime)));
            model.addAttribute("chanBranchData", extractBigDecimalData(orderRepo.findChannelTabRevenueByBranch(targetChannels, startTime, endTime)));

            model.addAttribute("chanTopProdLabels", extractStringLabels(orderRepo.findChannelTabTopProducts(targetChannels, startTime, endTime, PageRequest.of(0, 5))));
            model.addAttribute("chanTopProdData", extractLongData(orderRepo.findChannelTabTopProducts(targetChannels, startTime, endTime, PageRequest.of(0, 5))));

            model.addAttribute("chanReturnProdLabels", extractStringLabels(orderRepo.findChannelTabTopReturnedProducts(targetChannels, startTime, endTime, PageRequest.of(0, 5))));
            model.addAttribute("chanReturnProdData", extractLongData(orderRepo.findChannelTabTopReturnedProducts(targetChannels, startTime, endTime, PageRequest.of(0, 5))));
        }

        return "reports/report";
    }

    private void loadGeneralKPIs(Model model, LocalDateTime start, LocalDateTime end) {
        // 1. Tính Doanh thu và Giá vốn
        BigDecimal netRev = orderRepo.sumNetRevenue(start, end);
        BigDecimal cogs = orderRepo.sumTotalCOGS(start, end);
        if (netRev == null) netRev = BigDecimal.ZERO;
        if (cogs == null) cogs = BigDecimal.ZERO;

        // 2. Lợi nhuận gộp = Doanh thu thuần - Giá vốn
        BigDecimal grossProfit = netRev.subtract(cogs);

        // 3. TỔNG CHI PHÍ VẬN HÀNH (Lấy toàn bộ Phiếu Chi từ Sổ Quỹ)
        BigDecimal operatingExpenses = cashTransactionRepository.sumOperatingExpensesBetweenDates(start, end);
        if (operatingExpenses == null) operatingExpenses = BigDecimal.ZERO;

        // 4. BỔ SUNG: Lấy CHÍNH XÁC Thu nhập khác (Phiếu thu)
        BigDecimal otherIncome = cashTransactionRepository.sumOtherIncomeBetweenDates(start, end);
        if (otherIncome == null) otherIncome = BigDecimal.ZERO;

        // 4. LỢI NHUẬN RÒNG (Lợi nhuận gộp - Chi phí vận hành)
        BigDecimal netProfit = grossProfit.add(otherIncome).subtract(operatingExpenses);

        // 5. Ném ra View
        model.addAttribute("netRevenue", netRev);
        model.addAttribute("grossProfit", grossProfit);
        model.addAttribute("operatingExpenses", operatingExpenses);
        model.addAttribute("netProfit", netProfit);

        Long orders = orderRepo.countTotalOrders(start, end);
        model.addAttribute("totalOrders", orders != null ? orders : 0);
        BigDecimal invVal = inventoryRepo.sumTotalInventoryValue();
        model.addAttribute("inventoryValue", invVal != null ? invVal : BigDecimal.ZERO);
    }

    private void loadTimeCharts(Model model, LocalDateTime startTime, LocalDateTime endTime) {
        List<Object[]> revByDateRaw = orderRepo.findRevenueByDate(startTime, endTime);
        List<Object[]> orderByDateRaw = orderRepo.findOrderCountByDate(startTime, endTime);
        List<String> dateLabels = new ArrayList<>();
        List<BigDecimal> revData = new ArrayList<>();
        List<Long> orderData = new ArrayList<>();
        List<BigDecimal> aovData = new ArrayList<>();

        for (Object[] obj : revByDateRaw) {
            String date = obj[0].toString();
            BigDecimal rev = (BigDecimal) obj[1];
            dateLabels.add(date);
            revData.add(rev);
            Long count = orderByDateRaw.stream().filter(o -> o[0].toString().equals(date)).map(o -> ((Number) o[1]).longValue()).findFirst().orElse(0L);
            orderData.add(count);
            aovData.add(count > 0 ? rev.divide(BigDecimal.valueOf(count), 0, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        }
        model.addAttribute("dateLabels", dateLabels);
        model.addAttribute("revData", revData);
        model.addAttribute("orderData", orderData);
        model.addAttribute("aovData", aovData);
    }

    private void loadTopProducts(Model model, LocalDateTime startTime, LocalDateTime endTime) {
        List<Object[]> data = orderRepo.findTopSellingProducts(startTime, endTime, PageRequest.of(0, 5));
        model.addAttribute("topProdLabels", extractStringLabels(data));
        model.addAttribute("topProdData", extractLongData(data));
    }

    private void loadChannelAndBranchRevenue(Model model, LocalDateTime startTime, LocalDateTime endTime) {
        List<Object[]> chData = orderRepo.countOrdersByChannel(startTime, endTime);
        model.addAttribute("channelLabels", extractStringLabels(chData));
        model.addAttribute("channelData", extractLongData(chData));
        List<Object[]> brData = orderRepo.findRevenueByBranch(startTime, endTime);
        model.addAttribute("branchLabels", extractStringLabels(brData));
        model.addAttribute("branchData", extractBigDecimalData(brData));
    }

    private List<String> extractStringLabels(List<Object[]> list) {
        List<String> result = new ArrayList<>();
        for (Object[] obj : list) result.add(obj[0] != null ? obj[0].toString() : "Khác");
        return result;
    }

    private List<Long> extractLongData(List<Object[]> list) {
        List<Long> result = new ArrayList<>();
        for (Object[] obj : list) result.add(obj[1] != null ? ((Number) obj[1]).longValue() : 0L);
        return result;
    }

    private List<BigDecimal> extractBigDecimalData(List<Object[]> list) {
        List<BigDecimal> result = new ArrayList<>();
        for (Object[] obj : list) result.add(obj[1] != null ? (BigDecimal) obj[1] : BigDecimal.ZERO);
        return result;
    }
}