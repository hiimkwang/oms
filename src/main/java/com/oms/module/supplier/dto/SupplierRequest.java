package com.oms.module.supplier.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SupplierRequest {

    private String code; // Nếu trống, Backend sẽ tự sinh

    @NotBlank(message = "Tên nhà cung cấp không được để trống")
    private String name;

    // Liên hệ
    private String phone;
    private String email;
    private String taxCode;
    private String website;
    private String fax;

    // Địa chỉ
    private String country;
    private String province;
    private String district;
    private String ward;
    private String addressDetail;

    // Phân loại
    private String assignee;
    private String tags;
}