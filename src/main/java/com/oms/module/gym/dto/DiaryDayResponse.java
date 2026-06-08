package com.oms.module.gym.dto;

import com.oms.module.gym.entity.GymDailyLog;
import com.oms.module.gym.entity.GymDiaryEntry;

import java.util.List;

/**
 * Dữ liệu trả về cho 1 ngày: thông tin tổng hợp + danh sách món đã ăn + tổng macro.
 */
public record DiaryDayResponse(
        String date,
        GymDailyLog daily,
        List<GymDiaryEntry> entries,
        double totalCarb,
        double totalFat,
        double totalProtein,
        double totalCalo
) {
}
