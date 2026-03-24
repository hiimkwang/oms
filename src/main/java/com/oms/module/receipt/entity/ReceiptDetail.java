package com.oms.module.receipt.entity;

import com.oms.module.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "receipt_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiptDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_id", nullable = false)
    private Receipt receipt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product; // Liên kết tới Sản phẩm

    @Column(name = "quantity", nullable = false)
    private Integer quantity; // Số lượng nhập

    @Column(name = "import_price", nullable = false)
    private Double importPrice; // Đơn giá nhập (Giá vốn)

    @Column(name = "total_price")
    private Double totalPrice; // Thành tiền (quantity * importPrice)
}