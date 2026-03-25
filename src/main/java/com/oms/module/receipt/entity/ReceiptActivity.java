package com.oms.module.receipt.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "receipt_activities")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_id")
    private Receipt receipt;

    private String action; // Ví dụ: "Tạo đơn", "Nhập kho", "Thanh toán", "Hủy đơn"
    private String creatorName;

    @CreationTimestamp
    private LocalDateTime createdAt;
}