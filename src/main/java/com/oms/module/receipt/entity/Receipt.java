package com.oms.module.receipt.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "receipts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "receipt_code", unique = true, nullable = false)
    private String receiptCode; // Số phiếu nhập (VD: PN000001)

    @Column(name = "receipt_date")
    private LocalDate receiptDate; // Ngày nhập

    @Column(name = "supplier_name")
    private String supplierName; // Tên nhà cung cấp (VD: 深圳市博诚电脑科技有限公司)

    @Column(name = "importer")
    private String importer; // Người nhập (VD: Quang)

    @OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReceiptDetail> receiptDetails = new ArrayList<>();

    @Column(name = "total_amount")
    private Double totalAmount; // Tổng tiền nhập hàng

    @Column(name = "note")
    private String note; // Ghi chú

    @PrePersist
    protected void onCreate() {
        if (this.receiptDate == null) {
            this.receiptDate = LocalDate.now();
        }
    }
}