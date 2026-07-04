package com.oms.module.packing.dto;

import lombok.Data;

/**
 * Yêu cầu tạo đơn nháp từ trạm đóng gói.
 * Đơn được gắn vào khách "Khách lẻ" mặc định, ở trạng thái DRAFT để nhân viên soát lại sau.
 */
@Data
public class PackingOrderRequest {
    private String trackingCode;     // Mã vận đơn quét được
    private String recipientName;    // Tên người nhận (OCR hoặc nhập tay) - tùy chọn
    private String recipientPhone;   // SĐT người nhận - tùy chọn
    private String shippingAddress;  // Địa chỉ giao - tùy chọn
    private String note;             // Ghi chú
    private String salesChannelCode; // Nguồn đơn / kênh bán
    private Long branchId;           // Chi nhánh bán
    private String referenceCode;    // Mã đơn sàn (reference)
}
