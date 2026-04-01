package com.oms.module.cashbook.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cash_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code; // PT0001, PC0001

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    private TargetGroup targetGroup;

    private Long targetId; // ID của Khách/NCC/Nhân viên tương ứng
    private String targetName; // Lưu tên tại thời điểm thu/chi để làm báo cáo nhanh

    private BigDecimal amount;
    private String reason;
    private String description;
    private String referenceCode; // Mã đơn hàng hoặc mã hóa đơn liên quan

    @Column(name = "branch_id")
    private Long branchId;

    private LocalDateTime transactionDate;
    private String creatorName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(columnDefinition = "TEXT")
    private String attachments;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.transactionDate == null) this.transactionDate = LocalDateTime.now();
    }

    // Loại giao dịch
    public enum TransactionType {RECEIPT, PAYMENT} // Phiếu thu, Phiếu chi

    // Hình thức thanh toán
    public enum PaymentMethod {CASH, BANK} // Tiền mặt, Ngân hàng

    // Nhóm đối tượng
    public enum TargetGroup {CUSTOMER, SUPPLIER, EMPLOYEE, OTHER}
}
