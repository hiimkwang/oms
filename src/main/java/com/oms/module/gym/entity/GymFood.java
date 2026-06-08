package com.oms.module.gym.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Database món ăn (tham chiếu theo 100g). Tương ứng sheet "Thông Tin Đồ Ăn".
 */
@Entity
@Table(name = "gym_food")
@Data
@NoArgsConstructor
public class GymFood {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    // Các chỉ số tính trên 100 gram
    private double carb;
    private double fat;
    private double protein;
    private double calo;

    /**
     * Gợi ý khẩu phần (JSON), ví dụ: [{"label":"1 bát","grams":200},{"label":"1 chén","grams":100}].
     * Giúp người dùng ước lượng số gram khi nhập.
     */
    @Column(columnDefinition = "TEXT")
    private String portions;
}
