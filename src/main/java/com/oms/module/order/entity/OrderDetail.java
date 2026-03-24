package com.oms.module.order.entity;

import com.oms.module.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product; // Mã SP

    @Column(name = "quantity", nullable = false)
    private Integer quantity; // SL

    @Column(name = "unit_price", nullable = false)
    private Double unitPrice; // Đơn giá (lấy từ Product tại thời điểm bán)

    @Column(name = "discount")
    private Double discount; // Chiết khấu

    @Column(name = "total_price")
    private Double totalPrice; // Thành tiền = (unitPrice * quantity) - discount
}