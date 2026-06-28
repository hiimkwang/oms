package com.oms.module.report.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * 1 dòng trong báo cáo Bán chạy / Tồn đọng cho mỗi SKU.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventoryMovementRow {
    private String sku;
    private String productName;
    private String variantName;
    private String imageUrl;

    private int stock;            // Tồn hiện tại
    private long soldQty;         // SL đã bán trong kỳ
    private BigDecimal revenue;   // Doanh thu trong kỳ
    private BigDecimal costValue; // Vốn đang chôn trong tồn kho ( = tồn * giá vốn )

    private Integer daysOfStock;  // Số ngày bán hết tồn hiện tại (null = không xác định / hết hàng)

    // FAST = bán chạy, NORMAL = bình thường, SLOW = bán chậm, DEAD = tồn đọng (không bán được), SOLD_OUT = hết hàng
    private String category;
    private String categoryLabel;
}
