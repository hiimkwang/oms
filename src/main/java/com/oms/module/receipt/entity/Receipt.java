package com.oms.module.receipt.entity;

import com.oms.module.supplier.entity.Supplier; // Nhớ check path này
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Formula;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "receipts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Receipt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(name = "branch_id") // THÊM DÒNG NÀY
    private Long branchId;

    private String branchName; // Chi nhánh nhập
    private String creatorName; // Nhân viên tạo
    private BigDecimal totalAmount; // Tổng giá trị đơn
    @Formula("(SELECT COALESCE(SUM(d.quantity), 0) FROM receipt_details d WHERE d.receipt_id = id)")
    private Integer totalQuantity;
    private String note;
    private String paymentStatus; // PAID, UNPAID
    @OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("createdAt DESC") // Tự động sắp xếp timeline
    private List<ReceiptActivity> activities;

    // Thêm field này để fix lỗi chữ "Chưa nhập kho" bị đứng im
    private String importStatus; // PENDING, COMPLETED
    private String status;       // TRADING, CANCELLED
    @OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReceiptDetail> details;
    private BigDecimal itemsAmount;
    private BigDecimal discount;
    private BigDecimal shippingFee;
    private BigDecimal amountPaid;

    private LocalDateTime createdAt;
}