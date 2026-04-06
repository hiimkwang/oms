package com.oms.constant;

import java.time.format.DateTimeFormatter;

public class CommonConstants {
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static class OrderStatusConstant {
        // Trạng thái Đơn hàng (ORDER_STATUS)
        public static final String DRAFT = "DRAFT";           // Nháp
        public static final String CREATED = "CREATED";       // Khởi tạo
        public static final String CONFIRMED = "CONFIRMED";   // Đã xác nhận
        public static final String PROCESSING = "PROCESSING"; // Đang xử lý
        public static final String SHIPPING = "SHIPPING";     // Đang giao hàng
        public static final String COMPLETED = "COMPLETED";   // Hoàn thành
        public static final String CANCELLED = "CANCELLED";   // Đã hủy
        public static final String RETURNED = "RETURNED";     // Hoàn trả
    }

    public static class PaymentStatusConstant {
        // Trạng thái Thanh toán (PAYMENT_STATUS)
        public static final String UNPAID = "UNPAID";     // Chưa thanh toán
        public static final String PENDING = "PENDING";   // Đang chờ xử lý
        public static final String PARTIAL = "PARTIAL";   // Thanh toán một phần
        public static final String PAID = "PAID";         // Đã thanh toán
        public static final String REFUNDED = "REFUNDED"; // Đã hoàn tiền
    }

    public static class ReturnStatusConstant {
        // Trạng thái Phiếu trả hàng (RETURN_STATUS)
        public static final String PENDING = "PENDING";     // Đang chờ xử lý
        public static final String APPROVED = "APPROVED";   // Đã chấp nhận
        public static final String REJECTED = "REJECTED";   // Đã từ chối
        public static final String COMPLETED = "COMPLETED"; // Đã hoàn tất

        // Trạng thái cờ phụ trong Phiếu trả hàng
        public static final String RESTOCK_PENDING = "PENDING";
        public static final String RESTOCK_RESTOCKED = "RESTOCKED";
        public static final String REFUND_UNPAID = "UNPAID";
        public static final String REFUND_REFUNDED = "REFUNDED";
    }

    public static class GeneralStatusConstant {
        // Trạng thái Chung cho Đối tác, Khách hàng, Nhân viên (GENERAL_STATUS)
        public static final String ACTIVE = "ACTIVE";     // Đang hoạt động
        public static final String INACTIVE = "INACTIVE"; // Ngừng hoạt động
        public static final String BANNED = "BANNED";     // Bị đình chỉ
    }

    public static class ReceiptStatusConstant {
        public static final String TRADING = "TRADING";
        public static final String COMPLETED = "COMPLETED";
        public static final String CANCELLED = "CANCELLED";

        public static final String PENDING = "PENDING";     // Đang chờ xử lý

    }
}