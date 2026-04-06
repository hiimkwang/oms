package com.oms.module.setting.config;

import com.oms.constant.CommonConstants;
import com.oms.module.setting.service.MasterDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MasterDataSeeder implements CommandLineRunner {

    private final MasterDataService masterDataService;

    @Override
    public void run(String... args) throws Exception {
        // 1. Tự động nạp Danh mục
        if (masterDataService.getValuesByType("CATEGORY").isEmpty()) {
            List<String> defaultCategories = List.of("Bàn phím cơ", "Switch", "Keycap", "Phụ kiện", "Linh kiện Custom", "Dụng cụ lube");
            for (String category : defaultCategories) {
                // Tham số: Type, Value (viết hoa làm key), Label (hiển thị tiếng Việt)
                masterDataService.createIfNotExist("CATEGORY", category.toUpperCase(), category);
            }
            System.out.println("✅ Đã tự động nạp dữ liệu Danh mục vào DB!");
        }

        // 2. Tự động nạp Hãng sản xuất (Brand)
        if (masterDataService.getValuesByType("BRAND").isEmpty()) {
            List<String> defaultBrands = List.of("Aula", "Leobog", "Akko", "Xinmeng", "Cherry", "Kailh", "Gateron", "FL Esports", "Khác");
            for (String brand : defaultBrands) {
                masterDataService.createIfNotExist("BRAND", brand.toUpperCase(), brand);
            }
            System.out.println("✅ Đã tự động nạp dữ liệu Hãng sản xuất vào DB!");
        }

        // 3. Tự động nạp Đơn vị tính chuyên cho Mechkey
        if (masterDataService.getValuesByType("UNIT").isEmpty()) {
            List<String> defaultUnits = List.of("Chiếc", "Bộ", "Pack", "Lọ", "Tuýp", "Gram", "Sợi", "Tấm", "Cái");
            for (String unit : defaultUnits) {
                masterDataService.createIfNotExist("UNIT", unit.toUpperCase(), unit);
            }
            System.out.println("✅ Đã tự động nạp dữ liệu Đơn vị tính vào DB!");
        }

        // ==========================================
        // KHU VỰC CÁC TRẠNG THÁI (STATUS) ĐỒNG NHẤT
        // ==========================================

        // 4. Trạng thái Đơn hàng (Order Status)
        if (masterDataService.getValuesByType("ORDER_STATUS").isEmpty()) {
            Map<String, String> orderStatuses = new LinkedHashMap<>();
            orderStatuses.put("CREATED", "Khởi tạo");
            orderStatuses.put("CONFIRMED", "Đã xác nhận");
            orderStatuses.put("PROCESSING", "Đang xử lý");
            orderStatuses.put("SHIPPING", "Đang giao hàng");
            orderStatuses.put("COMPLETED", "Hoàn thành");
            orderStatuses.put("CANCELLED", "Đã hủy");
            orderStatuses.put("RETURNED", "Hoàn trả");

            orderStatuses.forEach((value, label) ->
                    masterDataService.createIfNotExist("ORDER_STATUS", value, label)
            );
            System.out.println("✅ Đã tự động nạp dữ liệu Trạng thái Đơn hàng vào DB!");
        }

        // 5. Trạng thái Thanh toán (Payment Status)
        if (masterDataService.getValuesByType("PAYMENT_STATUS").isEmpty()) {
            Map<String, String> paymentStatuses = new LinkedHashMap<>();
            paymentStatuses.put("UNPAID", "Chưa thanh toán");
            paymentStatuses.put("PARTIAL", "Thanh toán một phần");
            paymentStatuses.put("PAID", "Đã thanh toán");
            paymentStatuses.put("REFUNDED", "Đã hoàn tiền");

            paymentStatuses.forEach((value, label) ->
                    masterDataService.createIfNotExist("PAYMENT_STATUS", value, label)
            );
            System.out.println("✅ Đã tự động nạp dữ liệu Trạng thái Thanh toán vào DB!");
        }

        // 6. Trạng thái Đối tác / Nhà cung cấp / Khách hàng (General Status)
        if (masterDataService.getValuesByType("GENERAL_STATUS").isEmpty()) {
            Map<String, String> generalStatuses = new LinkedHashMap<>();
            generalStatuses.put("ACTIVE", "Đang hoạt động");
            generalStatuses.put("INACTIVE", "Ngừng hoạt động");
            generalStatuses.put("BANNED", "Đình chỉ");

            generalStatuses.forEach((value, label) ->
                    masterDataService.createIfNotExist("GENERAL_STATUS", value, label)
            );
            System.out.println("✅ Đã tự động nạp dữ liệu Trạng thái Chung vào DB!");
        }

        // 7. Trạng thái Trả hàng (Return Status)
        if (masterDataService.getValuesByType("RETURN_STATUS").isEmpty()) {
            Map<String, String> returnStatuses = new LinkedHashMap<>();
            returnStatuses.put("PENDING", "Chờ xử lý");
            returnStatuses.put("APPROVED", "Đã chấp nhận yêu cầu");
            returnStatuses.put("REJECTED", "Đã từ chối");
            returnStatuses.put("COMPLETED", "Hoàn tất trả hàng");

            returnStatuses.forEach((value, label) ->
                    masterDataService.createIfNotExist("RETURN_STATUS", value, label)
            );
            System.out.println("✅ Đã tự động nạp dữ liệu Trạng thái Trả hàng vào DB!");
        }

        if (masterDataService.getMasterDataByType("RECEIPT_STATUS").isEmpty()) {
            Map<String, String> receiptStatuses = new LinkedHashMap<>();
            receiptStatuses.put(CommonConstants.ReceiptStatusConstant.TRADING, "Đang giao dịch");
            receiptStatuses.put(CommonConstants.ReceiptStatusConstant.COMPLETED, "Hoàn thành");
            receiptStatuses.put(CommonConstants.ReceiptStatusConstant.CANCELLED, "Đã hủy");

            receiptStatuses.forEach((val, label) -> masterDataService.createIfNotExist("RECEIPT_STATUS", val, label));
        }
    }
}