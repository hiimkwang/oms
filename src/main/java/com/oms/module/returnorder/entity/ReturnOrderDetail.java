package com.oms.module.returnorder.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "return_order_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnOrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_order_id")
    private ReturnOrder returnOrder;

    private String sku;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice; // Đơn giá lúc mua
    private BigDecimal refundAmount; // Thành tiền (qty * unitPrice)
}