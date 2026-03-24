package com.oms.module.customer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_code", unique = true, nullable = false)
    private String customerCode; // MÃ KH (VD: SP_3dprintingsoul)

    @Column(name = "full_name", nullable = false)
    private String fullName; // KHÁCH HÀNG

    @Column(name = "company")
    private String company; // CÔNG TY / Kênh bán

    @Column(name = "address")
    private String address; // ĐỊA CHỈ

    @Column(name = "phone_number")
    private String phoneNumber; // SĐT

    @Column(name = "email")
    private String email; // EMAIL

    @Column(name = "tax_code")
    private String taxCode; // MST

    @Column(name = "created_at")
    private LocalDate createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDate.now();
    }
}