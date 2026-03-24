package com.oms.module.customer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CustomerRequest {
    @NotBlank(message = "Mã khách hàng không được để trống")
    private String customerCode;

    @NotBlank(message = "Tên khách hàng không được để trống")
    private String fullName;

    private String company;
    private String address;
    private String phoneNumber;
    private String email;
    private String taxCode;
}