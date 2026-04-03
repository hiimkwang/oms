package com.oms.module.order.entity;

import com.oms.module.customer.entity.Customer;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_code", unique = true, nullable = false)
    private String orderCode;

    // --- Thông tin chung ---
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "sales_channel_code")
    private String salesChannelCode;

    @Column(name = "branch_id")
    private Long branchId; // Chi nhánh bán hàng

    @Column(name = "status")
    private String status;

    @Column(name = "note")
    private String note;

    // --- Vận chuyển ---
    @Column(name = "shipping_type")
    private String shippingType;

    @Column(name = "ship_from_branch_id")
    private Long shipFromBranchId;

    @Column(name = "shipping_partner")
    private String shippingPartner;

    @Column(name = "tracking_code")
    private String trackingCode;

    @Column(name = "shipping_address", columnDefinition = "TEXT")
    private String shippingAddress;

    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    @Column(name = "shipping_fee")
    private BigDecimal shippingFee;

    @Column(name = "cod_amount")
    private BigDecimal codAmount;

    @Column(name = "ship_weight")
    private Double shipWeight;

    // --- Thanh toán ---
    @Column(name = "payment_status")
    private String paymentStatus;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "amount_paid")
    private BigDecimal amountPaid;

    // --- Hóa đơn VAT ---
    @Column(name = "invoice_tax_code")
    private String invoiceTaxCode;

    @Column(name = "invoice_company_name")
    private String invoiceCompanyName;

    @Column(name = "invoice_company_address")
    private String invoiceCompanyAddress;

    // --- Tiền nong ---
    @Column(name = "discount_amount")
    private BigDecimal discountAmount;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> details = new ArrayList<>();

    @Builder.Default
    @OrderBy("createdAt DESC") // Tự động sắp xếp mới nhất lên đầu
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderActivity> activities = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "reference_code", length = 100)
    private String referenceCode;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}