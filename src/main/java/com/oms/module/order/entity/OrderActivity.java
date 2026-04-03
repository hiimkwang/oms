package com.oms.module.order.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_activities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Dùng JsonIgnore để tránh lỗi lặp vô hạn (Infinite Recursion) khi trả JSON về Frontend
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "action")
    private String action; // VD: "Tạo mới", "Cập nhật", "Thanh toán"

    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // Chi tiết thao tác

    @Column(name = "created_by")
    private String createdBy; // Tên người thực hiện (hoặc "Hệ thống")

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}