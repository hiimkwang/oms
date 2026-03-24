package com.oms.module.cashbook.dto;

import com.oms.module.cashbook.entity.CashTransaction.TransactionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CashTransactionRequest {
    @NotBlank(message = "Số phiếu không được để trống")
    private String voucherCode;

    @NotNull(message = "Loại giao dịch (THU/CHI) không được để trống")
    private TransactionType transactionType;

    private LocalDate transactionDate;

    @NotBlank(message = "Tên người nộp/nhận không được để trống")
    private String personName;

    private String address;
    private String referenceDocument;
    private String description;

    @NotNull(message = "Số tiền không được để trống")
    @Min(value = 0, message = "Số tiền phải lớn hơn hoặc bằng 0")
    private Double amount;
}