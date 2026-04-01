package com.oms.module.order.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.oms.module.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    @JsonIgnore // Tránh lỗi lặp vô hạn (Infinite Recursion) khi trả JSON
    private Order order;

    // Cho phép null để lưu sản phẩm tùy chỉnh (Custom Product)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = true)
    private Product product;

    @Column(name = "sku")
    private String sku; // Lưu cứng SKU để đề phòng Product bị xóa mất

    @Column(name = "product_name")
    private String productName; // Lưu tên sản phẩm

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "discount")
    private BigDecimal discount;

    @Column(name = "total_price")
    private BigDecimal totalPrice;
    
    @Column(name = "note")
    private String note;

    @Column(name = "is_custom")
    private Boolean isCustom;

    @Column(name = "serial_number")
    private String serialNumber; // Lưu số Serial / IMEI

    @Column(name = "warranty_months")
    private Integer warrantyMonths; // Lưu số tháng bảo hành (VD: 12, 24)

    @Column(name = "warranty_start_date")
    private LocalDateTime warrantyStartDate; // Ngày kích hoạt bảo hành

    @Column(name = "warranty_end_date")
    private LocalDateTime warrantyEndDate; // Ngày hết hạn bảo hành

}