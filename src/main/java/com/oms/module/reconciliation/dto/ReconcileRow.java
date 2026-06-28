package com.oms.module.reconciliation.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 1 dòng kết quả đối soát giữa file của sàn và đơn trên OMS.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReconcileRow {
    // MATCHED = khớp; MISSING_OMS = sàn có nhưng OMS chưa tạo; MISSING_FILE = OMS có nhưng file sàn không có
    private String status;
    private String statusLabel;

    private String tracking;       // Mã vận chuyển
    private String sanOrderCode;   // Mã đơn trên sàn (nếu có trong file)
    private String sanStatus;      // Trạng thái đơn trên sàn (từ file)
    private String buyerId;        // Người mua (tài khoản sàn) - dùng đối chiếu & làm định danh khách
    private BigDecimal buyerPaid;  // Số tiền khách thanh toán (tham chiếu)
    private String omsOrderCode;   // Mã đơn trên OMS (nếu khớp)

    private BigDecimal sanAmount;  // Doanh thu sàn (giá trị đơn hàng)
    private BigDecimal fee;        // Tổng phí sàn (cố định + dịch vụ + xử lý GD)
    private BigDecimal netReceived;// Thực nhận ước tính = doanh thu - phí sàn
    private BigDecimal omsAmount;  // Số tiền đơn trên OMS
    private BigDecimal diff;       // Chênh lệch doanh thu = sàn - OMS

    private String note;

    // Dữ liệu để TẠO ĐƠN BÙ (chỉ có ở dòng MISSING_OMS)
    private String customerName;
    private String customerPhone;
    private String shippingAddress;
    private List<ReconcileItem> items;
}
