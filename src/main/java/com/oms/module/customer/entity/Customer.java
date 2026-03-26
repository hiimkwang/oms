package com.oms.module.customer.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

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

    private String fullName;
    private String phone;
    private String email;
    private String company;
    private String address;
    private String taxCode;
    private String customerGroup;
    private String note;

    @CreationTimestamp
    private LocalDateTime createdAt;
}