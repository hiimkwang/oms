package com.oms.module.gym.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Cấu hình cá nhân (1 bản ghi duy nhất, id = 1): mục tiêu macro hằng ngày
 * và các chỉ số cơ thể dùng cho máy tính "thời gian giảm cân".
 */
@Entity
@Table(name = "gym_setting")
@Data
@NoArgsConstructor
public class GymSetting {
    @Id
    private Long id = 1L;

    // --- Mục tiêu dinh dưỡng hằng ngày ---
    private double goalCalories = 2000;
    private double goalCarb = 200;     // gram
    private double goalFat = 55;       // gram
    private double goalProtein = 175;  // gram

    // --- Chỉ số cơ thể cho máy tính giảm cân ---
    private double currentWeight = 80;   // kg
    private double bodyFatPct = 0.25;    // tỉ lệ mỡ (0..1)
    private double targetBodyFatPct = 0.12;
    private double weeklyLossPct = 0.007; // % cân nặng giảm mỗi tuần
    private double height = 170;          // cm
    private int age = 28;
    private String gender = "MALE";       // MALE / FEMALE
    private double activityFactor = 1.55; // hệ số vận động (TDEE)
}
