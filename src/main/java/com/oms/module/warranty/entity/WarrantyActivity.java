package com.oms.module.warranty.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "warranty_activities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarrantyActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private WarrantyTicket ticket;

    private String action; // Tên hành động (VD: "Cập nhật trạng thái", "Cập nhật ghi chú")

    @Column(columnDefinition = "TEXT")
    private String description; // Chi tiết thay đổi

    private String creatorName; // Người thực hiện

    @CreationTimestamp
    private LocalDateTime createdAt;
}