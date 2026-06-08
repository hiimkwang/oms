package com.oms.module.gym.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Thư viện bài tập: mỗi bài định nghĩa 1 lần kèm ảnh minh họa, nhóm cơ, ghi chú.
 * Lịch tập (GymWorkoutDay) tham chiếu bài theo tên -> tự hiển thị ảnh tương ứng.
 */
@Entity
@Table(name = "gym_exercise")
@Data
@NoArgsConstructor
public class GymExercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    /** URL ảnh/GIF minh họa (upload qua /api/gym/upload hoặc dán link ngoài) */
    @Column(columnDefinition = "TEXT")
    private String imageUrl;

    /** Nhóm cơ chính: Ngực, Lưng, Chân, Vai, Tay, Bụng... */
    private String muscleGroup;

    @Column(columnDefinition = "TEXT")
    private String note;
}
