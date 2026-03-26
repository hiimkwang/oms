package com.oms.module.order.entity;

import com.oms.module.customer.entity.Customer;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_code", unique = true, nullable = false)
    private String orderCode; // Số phiếu (VD: 26031574S25MU0)

    @Column(name = "sales_channel")
    private String salesChannel; // Kênh bán hàng (Shopee, Web...)

    @Column(name = "status")
    private String status; // Trạng thái đơn (Đã hoàn thành, Đang giao...)

    @Column(name = "payment_method")
    private String paymentMethod; // Phương thức TT (Ví điện tử, COD...)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer; // Mã KH

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetails = new ArrayList<>();

    @Column(name = "total_amount")
    private BigDecimal totalAmount; // Tổng tiền thanh toán thực

    @Column(name = "note")
    private String note; // Ghi chú
    @Column(name = "created_at") // Map với cột dưới DB
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}