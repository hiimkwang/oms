package com.oms.module.receipt.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "receipt_details")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "receipt_id")
    private Receipt receipt;

    private String sku;
    private String productName;
    private Integer quantity;
    private BigDecimal importPrice; // Giá nhập tại thời điểm đó
}