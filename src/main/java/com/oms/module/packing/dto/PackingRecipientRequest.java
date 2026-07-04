package com.oms.module.packing.dto;

import lombok.Data;

/** Cập nhật thông tin người nhận cho đơn đóng gói (sau khi OCR / sửa tay). */
@Data
public class PackingRecipientRequest {
    private String recipientName;
    private String recipientPhone;
    private String shippingAddress;
    private String note;
    private String salesChannelCode; // Nguồn đơn / kênh bán
    private Long branchId;           // Chi nhánh bán
    private String referenceCode;    // Mã đơn sàn (reference)
}
