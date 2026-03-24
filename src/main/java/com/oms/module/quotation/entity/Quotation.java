package com.oms.module.quotation.entity;

import com.oms.module.customer.entity.Customer;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quotations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quotation_code", unique = true, nullable = false)
    private String quotationCode; // Số Báo Giá (VD: BG003)

    @Column(name = "quotation_date", nullable = false)
    private LocalDate quotationDate; // Ngày báo

    @Column(name = "valid_until")
    private LocalDate validUntil; // Ngày hết hiệu lực

    @Column(name = "staff_name")
    private String staffName; // Nhân viên báo giá

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer; // Khách hàng

    @OneToMany(mappedBy = "quotation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuotationDetail> quotationDetails = new ArrayList<>();

    @Column(name = "total_amount")
    private Double totalAmount; // Tổng tiền hàng

    @Column(name = "tax_percent")
    private Double taxPercent; // % Thuế GTGT (nếu có, VD: 10.0)

    @Column(name = "grand_total")
    private Double grandTotal; // Tổng cộng tiền thanh toán

    @Column(name = "status")
    private String status; // Tình trạng (VD: Chờ duyệt, Đã chốt, Đã hủy)

    @Column(name = "note", columnDefinition = "TEXT")
    private String note; // Ghi chú / Điều kiện thương mại

    @PrePersist
    protected void onCreate() {
        if (this.quotationDate == null) {
            this.quotationDate = LocalDate.now();
        }
        if (this.validUntil == null) {
            this.validUntil = this.quotationDate.plusDays(7); // Mặc định báo giá có hiệu lực 7 ngày
        }
    }
}