package com.oms.module.customer.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_groups")
@Data
public class CustomerGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    private String note;

    @CreationTimestamp
    private LocalDateTime createdAt;
    private Boolean autoUpdate;
    @Column(name = "color_code", length = 10)
    private String colorCode;
    @Column(columnDefinition = "TEXT")
    private String conditions; // Lưu chuỗi JSON chứa các điều kiện
}