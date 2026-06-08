package com.oms.module.gym.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Một dòng đồ ăn đã nạp trong ngày (tương ứng các dòng "Food Items" của sheet "Today").
 * Macro được lưu snapshot tại thời điểm thêm (đã nhân theo số gram).
 */
@Entity
@Table(name = "gym_diary_entry", indexes = @Index(name = "idx_diary_date", columnList = "log_date"))
@Data
@NoArgsConstructor
public class GymDiaryEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(nullable = false)
    private String foodName;

    private double grams;

    // Macro thực tế của khẩu phần (đã tính theo grams)
    private double carb;
    private double fat;
    private double protein;
    private double calo;
}
