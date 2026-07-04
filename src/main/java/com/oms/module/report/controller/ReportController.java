package com.oms.module.report.controller;

import com.oms.constant.CommonConstants;
import com.oms.module.cashbook.repository.CashTransactionRepository;
import com.oms.module.inventory.repository.InventoryRepository;
import com.oms.module.order.repository.OrderRepository;
import com.oms.module.report.dto.InventoryMovementRow;
import com.oms.module.report.service.ReportService;
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
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ReportController {

    private final OrderRepository orderRepo;
    private final InventoryRepository inventoryRepo;
    private final CashTransactionRepository cashTransactionRepository;
    private final ReportService reportService;

    @GetMapping("/ui/reports")
    public String reportOverview(Model model, @RequestParam(required = false, defaultValue = "overview") String tab, @RequestParam(required = false, defaultValue = "30days") String preset, @RequestParam(required = false) String channel,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = start;
        LocalDateTime endTime = end;

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

        // Phòng NPE: preset=custom nhưng client không truyền start/end -> mặc định 30 ngày gần nhất
        if (startTime == null) startTime = now.minusDays(30).with(LocalTime.MIN);
        if (endTime == null) endTime = now.with(LocalTime.MAX);

        model.addAttribute("preset", preset);
        model.addAttribute("startDate", startTime);
        model.addAttribute("endDate", endTime);
        model.addAttribute("tab", tab);
        model.addAttribute("selectedChannel", channel);

        if ("overview".equals(tab)) {
            loadGeneralKPIs(model, startTime, endTime);
            loadTimeCharts(model, startTime, endTime);
            loadTopProducts(model, startTime, endTime);
            loadChannelAndBranchRevenue(model, startTime, endTime);

        } else if ("revenue".equals(tab)) {
            loadGeneralKPIs(model, startTime, endTime);
            loadTimeCharts(model, startTime, endTime);
            loadTopProducts(model, startTime, endTime);
            loadChannelAndBranchRevenue(model, startTime, endTime);

            // XÓA DÒNG NÀY: List<Object[]> profitByDateRaw = orderRepo.findProfitByDate(startTime, endTime);
            List<BigDecimal> marginData = new ArrayList<>();
            List<BigDecimal> revData = (List<BigDecimal>) model.getAttribute("revData");

            // LẤY DỮ LIỆU ĐÃ ĐƯỢC ĐỒNG BỘ TỪ loadTimeCharts
            List<BigDecimal> profitData = (List<BigDecimal>) model.getAttribute("profitData");

            if (revData != null && profitData != null && !revData.isEmpty()) {
                for (int i = 0; i < revData.size(); i++) {
                    BigDecimal profit = profitData.get(i);
                    BigDecimal rev = revData.get(i);
                    marginData.add(rev.compareTo(BigDecimal.ZERO) > 0 ?
                            profit.divide(rev, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")) : BigDecimal.ZERO);
                }
            }
            model.addAttribute("marginData", marginData);

            // Lợi nhuận gộp theo KÊNH/CHI NHÁNH — CÙNG cơ sở order-level (Doanh thu − COGS) như KPI,
            // và CĂN ĐÚNG theo nhãn (channelLabels/branchLabels) để không vẽ lệch cột.
            @SuppressWarnings("unchecked")
            List<String> channelLabels = (List<String>) model.getAttribute("channelLabels");
            @SuppressWarnings("unchecked")
            List<String> branchLabels = (List<String>) model.getAttribute("branchLabels");

            model.addAttribute("channelProfitData", profitAlignedToLabels(channelLabels,
                    orderRepo.findRevenueByChannel(startTime, endTime),
                    orderRepo.findCogsByChannel(startTime, endTime)));
            model.addAttribute("branchProfitData", profitAlignedToLabels(branchLabels,
                    orderRepo.findRevenueByBranch(startTime, endTime),
                    orderRepo.findCogsByBranch(startTime, endTime)));

        } else if ("customer".equals(tab)) {
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
            model.addAttribute("chanCanceled", orderRepo.countChannelTabOrdersByStatus(targetChannels, CommonConstants.OrderStatusConstant.CANCELLED, startTime, endTime));
            model.addAttribute("chanReturned", orderRepo.countChannelTabOrdersByStatus(targetChannels, CommonConstants.OrderStatusConstant.RETURNED, startTime, endTime));
            model.addAttribute("chanShipped", orderRepo.countChannelTabOrdersByStatus(targetChannels, CommonConstants.OrderStatusConstant.COMPLETED, startTime, endTime));

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

    // BÁO CÁO BÁN CHẠY / TỒN ĐỌNG (thuộc Quản lý kho)
    @GetMapping("/ui/stock-movement")
    public String inventoryMovement(Model model,
                                    @RequestParam(required = false, defaultValue = "30days") String preset,
                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = start;
        LocalDateTime endTime = (end != null) ? end : now.with(LocalTime.MAX);

        if (!"custom".equals(preset) || startTime == null) {
            endTime = now.with(LocalTime.MAX);
            switch (preset) {
                case "7days":
                    startTime = now.minusDays(7).with(LocalTime.MIN);
                    break;
                case "thisMonth":
                    startTime = now.withDayOfMonth(1).with(LocalTime.MIN);
                    break;
                case "90days":
                    startTime = now.minusDays(90).with(LocalTime.MIN);
                    break;
                case "30days":
                default:
                    startTime = now.minusDays(30).with(LocalTime.MIN);
                    break;
            }
        }

        List<InventoryMovementRow> rows = reportService.getInventoryMovement(startTime, endTime);

        // Tổng vốn đang chôn trong tồn đọng (DEAD + SLOW)
        BigDecimal deadCapital = BigDecimal.ZERO;
        int fastCount = 0, deadCount = 0, slowCount = 0;
        for (InventoryMovementRow r : rows) {
            if ("DEAD".equals(r.getCategory()) || "SLOW".equals(r.getCategory())) {
                deadCapital = deadCapital.add(r.getCostValue() != null ? r.getCostValue() : BigDecimal.ZERO);
            }
            if ("FAST".equals(r.getCategory())) fastCount++;
            else if ("DEAD".equals(r.getCategory())) deadCount++;
            else if ("SLOW".equals(r.getCategory())) slowCount++;
        }

        model.addAttribute("rows", rows);
        model.addAttribute("preset", preset);
        model.addAttribute("startDate", startTime);
        model.addAttribute("endDate", endTime);
        model.addAttribute("deadCapital", deadCapital);
        model.addAttribute("fastCount", fastCount);
        model.addAttribute("deadCount", deadCount);
        model.addAttribute("slowCount", slowCount);
        return "reports/inventory-movement";
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

    private void loadTimeCharts(Model model, LocalDateTime start, LocalDateTime end) {
        List<String> dateLabels = new ArrayList<>();
        List<BigDecimal> revData = new ArrayList<>();
        List<Long> orderData = new ArrayList<>();
        List<BigDecimal> aovData = new ArrayList<>();
        List<BigDecimal> profitData = new ArrayList<>(); // Mảng chứa lợi nhuận thực tế

        // Đếm số ngày theo LỊCH (không theo số giờ) để không bị thiếu/thừa bucket ngày cuối
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate());

        // NẾU LỌC DƯỚI 35 NGÀY -> VẼ THEO TỪNG NGÀY
        if (daysBetween <= 35) {
            for (int i = 0; i <= daysBetween; i++) {
                LocalDateTime dayStart = start.plusDays(i).with(LocalTime.MIN);
                LocalDateTime dayEnd = dayStart.with(LocalTime.MAX);

                BigDecimal dRev = orderRepo.sumNetRevenue(dayStart, dayEnd);
                if (dRev == null) dRev = BigDecimal.ZERO;

                BigDecimal dCogs = orderRepo.sumTotalCOGS(dayStart, dayEnd);
                if (dCogs == null) dCogs = BigDecimal.ZERO;

                BigDecimal dOpExp = cashTransactionRepository.sumOperatingExpensesBetweenDates(dayStart, dayEnd);
                if (dOpExp == null) dOpExp = BigDecimal.ZERO;

                BigDecimal dOther = cashTransactionRepository.sumOtherIncomeBetweenDates(dayStart, dayEnd);
                if (dOther == null) dOther = BigDecimal.ZERO;

                Long dCount = orderRepo.countTotalOrders(dayStart, dayEnd);
                if (dCount == null) dCount = 0L;

                dateLabels.add(dayStart.getDayOfMonth() + "/" + dayStart.getMonthValue());
                revData.add(dRev);
                orderData.add(dCount);
                aovData.add(dCount > 0 ? dRev.divide(BigDecimal.valueOf(dCount), 0, RoundingMode.HALF_UP) : BigDecimal.ZERO);
                profitData.add(dRev.subtract(dCogs).add(dOther).subtract(dOpExp));
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

                BigDecimal mOpExp = cashTransactionRepository.sumOperatingExpensesBetweenDates(mStart, mEnd);
                if (mOpExp == null) mOpExp = BigDecimal.ZERO;

                BigDecimal mOther = cashTransactionRepository.sumOtherIncomeBetweenDates(mStart, mEnd);
                if (mOther == null) mOther = BigDecimal.ZERO;

                Long mCount = orderRepo.countTotalOrders(mStart, mEnd);
                if (mCount == null) mCount = 0L;

                dateLabels.add("T" + startMonth.getMonthValue() + "/" + startMonth.getYear());
                revData.add(mRev);
                orderData.add(mCount);
                aovData.add(mCount > 0 ? mRev.divide(BigDecimal.valueOf(mCount), 0, RoundingMode.HALF_UP) : BigDecimal.ZERO);
                profitData.add(mRev.subtract(mCogs).add(mOther).subtract(mOpExp));

                startMonth = startMonth.plusMonths(1);
            }
        }

        model.addAttribute("dateLabels", dateLabels);
        model.addAttribute("revData", revData);
        model.addAttribute("orderData", orderData);
        model.addAttribute("aovData", aovData);
        model.addAttribute("profitData", profitData); // Đẩy profitData ra model
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

    // Gom [label -> BigDecimal] từ kết quả GROUP BY
    private java.util.Map<String, BigDecimal> toAmountMap(List<Object[]> list) {
        java.util.Map<String, BigDecimal> map = new java.util.HashMap<>();
        for (Object[] obj : list) {
            String key = obj[0] != null ? obj[0].toString() : "Khác";
            BigDecimal val = obj[1] != null ? (BigDecimal) obj[1] : BigDecimal.ZERO;
            map.merge(key, val, BigDecimal::add);
        }
        return map;
    }

    // Lợi nhuận gộp = doanh thu − COGS, TRẢ VỀ theo đúng thứ tự labels (căn khớp biểu đồ)
    private List<BigDecimal> profitAlignedToLabels(List<String> labels, List<Object[]> revList, List<Object[]> cogsList) {
        List<BigDecimal> out = new ArrayList<>();
        if (labels == null) return out;
        java.util.Map<String, BigDecimal> rev = toAmountMap(revList);
        java.util.Map<String, BigDecimal> cogs = toAmountMap(cogsList);
        for (String label : labels) {
            BigDecimal r = rev.getOrDefault(label, BigDecimal.ZERO);
            BigDecimal c = cogs.getOrDefault(label, BigDecimal.ZERO);
            out.add(r.subtract(c));
        }
        return out;
    }
}