package com.oms.module.cashbook.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

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

    @Column(name = "voucher_code", unique = true, nullable = false)
    private String voucherCode; // Số phiếu thu/chi (VD: PT001, PC005)

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType; // Phân loại: THU hoặc CHI

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate; // Ngày chứng từ

    @Column(name = "person_name", nullable = false)
    private String personName; // Người Nộp / Người Nhận Tiền

    @Column(name = "address")
    private String address; // Địa Chỉ

    @Column(name = "reference_document")
    private String referenceDocument; // Chứng Từ (VD: Số Đơn hàng hoặc Số Phiếu nhập)

    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // Nội Dung Diễn Giải

    @Column(name = "amount", nullable = false)
    private Double amount; // Số Tiền

    @PrePersist
    protected void onCreate() {
        if (this.transactionDate == null) {
            this.transactionDate = LocalDate.now();
        }
    }

    public enum TransactionType {
        THU, CHI
    }
}