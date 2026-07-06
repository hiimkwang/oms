package com.oms.utility;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

/**
 * Bộ giải mã khoảng thời gian dùng chung cho MỌI bộ lọc (đơn hàng, sổ quỹ, báo cáo, nhập hàng, trả hàng, bảo hành...).
 * Chuẩn hoá 9 preset: today, yesterday, thisWeek, lastWeek, thisMonth, lastMonth, thisYear, lastYear, custom.
 */
public final class DateRangeUtil {

    private DateRangeUtil() {
    }

    public record DateRange(LocalDateTime start, LocalDateTime end) {
    }

    /**
     * Giải preset thành khoảng [start, end]. Với preset "custom" (hoặc không nhận diện được) sẽ dùng start/end truyền vào.
     * Nếu start/end vẫn null (client không gửi) thì mặc định 30 ngày gần nhất để phòng NPE.
     */
    public static DateRange resolve(String preset, LocalDateTime start, LocalDateTime end) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = start;
        LocalDateTime endTime = end;

        if (preset != null && !"custom".equals(preset)) {
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
                    startTime = now.with(DayOfWeek.MONDAY).with(LocalTime.MIN);
                    break;
                case "lastWeek":
                    startTime = now.minusWeeks(1).with(DayOfWeek.MONDAY).with(LocalTime.MIN);
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

        if (startTime == null) startTime = now.minusDays(30).with(LocalTime.MIN);
        if (endTime == null) endTime = now.with(LocalTime.MAX);

        return new DateRange(startTime, endTime);
    }
}
