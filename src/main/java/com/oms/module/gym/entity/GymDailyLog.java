package com.oms.module.gym.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Thông tin tổng hợp theo ngày: cân nặng, loại buổi tập, ghi chú.
 * Mỗi ngày tối đa 1 bản ghi.
 */
@Entity
@Table(name = "gym_daily_log")
@Data
@NoArgsConstructor
public class GymDailyLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "log_date", nullable = false, unique = true)
    private LocalDate logDate;

    /** Loại ngày tập: PUSH, PULL, LEG, REST, FREE... (tự do) */
    private String dayType;

    /** Cân nặng đo trong ngày (kg) - có thể null nếu không cân */
    private Double weight;

    @Column(columnDefinition = "TEXT")
    private String note;
}
