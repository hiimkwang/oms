package com.oms.module.receipt.entity;

import com.oms.module.supplier.entity.Supplier; // Nhớ check path này
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
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

    private String branchName; // Chi nhánh nhập
    private String creatorName; // Nhân viên tạo
    private BigDecimal totalAmount; // Tổng giá trị đơn
    private Integer totalQuantity; // Tổng số lượng nhập
    private String note;
    private String paymentStatus; // PAID, UNPAID

    @OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL)
    private List<ReceiptDetail> details;

    @CreationTimestamp
    private LocalDateTime createdAt;
}