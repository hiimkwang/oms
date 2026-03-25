package com.oms.module.receipt.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ReceiptRequest {
    private String supplierCode;
    private String branchName;
    private List<ItemRequest> items;
    private BigDecimal totalAmount;
    private String note;
    private String paymentStatus;

    @Data
    public static class ItemRequest {
        private String sku;
        private Integer quantity;
        private BigDecimal importPrice;
    }
}