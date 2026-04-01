package com.oms.module.cashbook.dto;

import com.oms.module.cashbook.entity.CashTransaction;
import com.oms.module.cashbook.entity.CashTransaction.TransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CashTransactionRequest {
    private TransactionType type;
    private CashTransaction.PaymentMethod paymentMethod;
    private CashTransaction.TargetGroup targetGroup;
    private Long targetId;
    private BigDecimal amount;
    private String reason;
    private String description;
    private Long branchId;
    private String code;
    private String referenceCode;
    private LocalDateTime transactionDate;
}