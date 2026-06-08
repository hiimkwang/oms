package com.oms.module.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerRequest {
    // Thông tin cơ bản
    private String customerCode;
    private String firstName;
    private String lastName;
    private String fullName; // Ghép từ firstName + lastName
    private String email;
    private String phoneNumber;
    private LocalDate dob;
    private String gender;
    private Boolean receiveAds;
    private String colorCode;
    // Phân loại & Ghi chú
    private String customerGroup;
    private String note;
    private String tags;

    // Địa chỉ nhận hàng (Shipping)
    private String shipFirstName;
    private String shipLastName;
    private String shipCompany;
    private String shipPhone;
    private String shipCountry;
    private String shipZip;
    private String shipCity;
    private String shipDistrict;
    private String shipWard;
    private String shipAddressDetail;

    // Thông tin hóa đơn (Invoice)
    private Boolean hasInvoice;
    private String taxCode;
    private String companyName;
    private String companyAddress;
    private String buyerName;
    private String citizenId;
    private String budgetCode;
    private String invoicePhone;
    private String invoiceEmail;
    private Long orderCount;
    private LocalDateTime lastOrder;
    private BigDecimal totalSpent;
}