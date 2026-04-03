package com.oms.module.returnorder.entity;

import com.oms.module.order.entity.Order;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "return_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "return_code", unique = true, nullable = false)
    private String returnCode;

    // Liên kết với Đơn hàng gốc
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false)
    private Order originalOrder;

    private String reason; // Lý do trả

    @Column(columnDefinition = "TEXT")
    private String note; // Ghi chú

    private BigDecimal returnFee; // Phí trả hàng (Shop chịu)
    private BigDecimal totalRefundAmount; // Tổng tiền cần hoàn cho khách

    // Trạng thái xử lý
    private String refundStatus;  // UNPAID (Chưa hoàn), REFUNDED (Đã hoàn tiền)
    private String restockStatus; // PENDING (Chưa nhập), RESTOCKED (Đã nhập kho)
    private String status;        // PROCESSING (Đang xử lý), COMPLETED (Hoàn tất), CANCELLED (Đã hủy)

    @Column(name = "created_by")
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Builder.Default
    @OneToMany(mappedBy = "returnOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReturnOrderDetail> details = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "returnOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<ReturnActivity> activities = new ArrayList<>();
}