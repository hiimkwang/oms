package com.oms.module.reconciliation.dto;

import lombok.Data;

import java.util.List;

/** Yêu cầu đồng bộ kết quả đối soát về OMS. */
@Data
public class ReconcileSyncRequest {

    private String channel;        // Kênh bán gán cho đơn tạo bù (vd SHOPEE)
    private String createStatus;   // Trạng thái đơn bù: DRAFT (mặc định) / COMPLETED / CONFIRMED

    private List<CreateOrder> createOrders;   // Đơn cần tạo bù (Sàn có, OMS chưa có)
    private List<FillCode> fillCodes;         // Đơn khớp cần điền bù mã đơn sàn / mã vận đơn
    private List<String> markPaidOrderCodes;  // Đơn khớp cần đánh dấu đã thanh toán

    @Data
    public static class CreateOrder {
        private String customerName;
        private String customerPhone;
        private String shippingAddress;
        private String referenceCode;   // Mã đơn sàn
        private String trackingCode;    // Mã vận đơn
        private List<ReconcileItem> items;
    }

    @Data
    public static class FillCode {
        private String omsOrderCode;
        private String referenceCode;
        private String trackingCode;
    }
}
