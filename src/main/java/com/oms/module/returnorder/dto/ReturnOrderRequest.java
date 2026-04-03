package com.oms.module.returnorder.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ReturnOrderRequest {
    private String originalOrderCode;
    private String reason;
    private String note;
    private BigDecimal returnFee;
    private List<ReturnItemRequest> details;

    @Data
    public static class ReturnItemRequest {
        private String sku;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
    }
}