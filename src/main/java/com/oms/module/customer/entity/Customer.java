package com.oms.module.customer.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Formula;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
@Data
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    private String firstName;
    private String lastName;
    private String fullName;
    private String phone;
    private String email;
    private LocalDate dob;
    private String gender; // NAM / NU

    private String customerGroup;
    private String note;
    private String tags;

    // Địa chỉ nhận hàng
    private String shipCompany;
    private String shipCity;
    private String shipDistrict;
    private String shipAddressDetail;

    // Thông tin hóa đơn
    private Boolean hasInvoice;
    private String taxCode;
    private String companyName; // Tên công ty xuất HĐ
    private String companyAddress;

    @CreationTimestamp
    private LocalDateTime createdAt;
    @Formula("(SELECT COUNT(o.id) FROM orders o WHERE o.customer_id = id AND o.status = 'Hoàn thành')")
    private Integer orderCount;
    @Formula("(SELECT COALESCE(SUM(o.total_amount), 0) FROM orders o WHERE o.customer_id = id AND o.status = 'Hoàn thành')")
    private Double totalSpent;
}