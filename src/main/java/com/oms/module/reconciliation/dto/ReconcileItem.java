package com.oms.module.reconciliation.dto;

import lombok.*;

import java.math.BigDecimal;

/** 1 dòng sản phẩm của đơn lấy từ file sàn (phục vụ tạo đơn bù). */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReconcileItem {
    private String sku;
    private String name;
    private int quantity;
    private BigDecimal unitPrice;
}
