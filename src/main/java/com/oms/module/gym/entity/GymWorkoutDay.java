package com.oms.module.gym.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lịch tập cố định theo thứ trong tuần (tương ứng sheet "Lịch tập").
 * dayOfWeek: 1 = Thứ 2 ... 7 = Chủ nhật.
 */
@Entity
@Table(name = "gym_workout_day")
@Data
@NoArgsConstructor
public class GymWorkoutDay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "day_of_week", nullable = false, unique = true)
    private int dayOfWeek;

    /** Nhóm buổi tập: Push / Pull / Leg / Rest... */
    private String program;

    /** Danh sách bài tập, mỗi bài 1 dòng (ngăn cách bằng \n) */
    @Column(columnDefinition = "TEXT")
    private String exercises;
}
