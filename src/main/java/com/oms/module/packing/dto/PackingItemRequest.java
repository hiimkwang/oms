package com.oms.module.packing.dto;

import lombok.Data;

/**
 * Một lần quét sản phẩm tại trạm đóng gói. Mã barcode = SKU biến thể trong OMS.
 */
@Data
public class PackingItemRequest {
    private String sku;            // Mã vạch quét được = SKU biến thể
    private String serialNumber;   // Serial/IMEI (tùy chọn)
    private Integer quantity;      // Mặc định 1 nếu null
}
