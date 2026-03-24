package com.oms.module.receipt.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ReceiptRequest {
    @NotBlank(message = "Số phiếu nhập không được để trống")
    private String receiptCode;

    private LocalDate receiptDate;
    private String supplierName;
    private String importer;
    private String note;

    @NotEmpty(message = "Phiếu nhập phải có ít nhất 1 sản phẩm")
    @Valid
    private List<ReceiptDetailRequest> details;
}