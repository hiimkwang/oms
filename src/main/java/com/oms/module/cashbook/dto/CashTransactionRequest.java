package com.oms.module.cashbook.dto;

import com.oms.module.cashbook.entity.CashTransaction;
import com.oms.module.cashbook.entity.CashTransaction.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CashTransactionRequest {
    @NotNull(message = "Loại phiếu (Thu/Chi) không được trống")
    private TransactionType type;
    private CashTransaction.PaymentMethod paymentMethod;
    private CashTransaction.TargetGroup targetGroup;
    private Long targetId;
    @NotNull(message = "Số tiền không được trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Số tiền phải lớn hơn 0")
    private BigDecimal amount;
    private String reason;
    private String description;
    private Long branchId;
    private String code;
    private String referenceCode;
    private LocalDateTime transactionDate;
}