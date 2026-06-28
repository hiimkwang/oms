package com.oms.module.inventory.service;

import com.oms.module.inventory.repository.InventoryRepository;
import com.oms.module.notification.entity.Notification;
import com.oms.module.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Tự động quét tồn kho mỗi sáng và rung chuông cảnh báo các sản phẩm sắp hết hàng.
 * Ngưỡng cảnh báo cấu hình tại application.properties: app.inventory.low-stock-threshold (mặc định 5).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LowStockScheduler {

    private final InventoryRepository inventoryRepository;
    private final NotificationService notificationService;

    @Value("${app.inventory.low-stock-threshold:5}")
    private int lowStockThreshold;

    // Chạy mỗi ngày lúc 8:00 sáng
    @Scheduled(cron = "${app.inventory.low-stock-cron:0 0 8 * * *}")
    public void checkLowStock() {
        try {
            List<Object[]> items = inventoryRepository.findLowStockItems(lowStockThreshold);
            if (items == null || items.isEmpty()) {
                return;
            }

            StringBuilder sb = new StringBuilder();
            int shown = 0;
            for (Object[] row : items) {
                if (shown >= 5) break;
                String name = row[1] != null ? row[1].toString() : (row[0] != null ? row[0].toString() : "SP");
                String variant = row[2] != null ? row[2].toString() : "";
                if (!variant.isBlank()) name = name + " - " + variant;
                Object total = row[3];
                if (shown > 0) sb.append(", ");
                sb.append(name).append(" (").append(total).append(")");
                shown++;
            }
            String more = items.size() > shown ? " và " + (items.size() - shown) + " sản phẩm khác" : "";

            String title = "Cảnh báo tồn kho thấp: " + items.size() + " sản phẩm";
            String message = "Tồn kho ≤ " + lowStockThreshold + ": " + sb + more + ". Hãy lên kế hoạch nhập hàng để tránh đứt hàng.";

            notificationService.create(title, message, Notification.NotificationType.WARNING, "/ui/inventory?stockStatus=in_stock");
            log.info("Đã gửi cảnh báo tồn kho thấp cho {} sản phẩm", items.size());
        } catch (Exception e) {
            log.error("Lỗi khi quét cảnh báo tồn kho thấp: {}", e.getMessage(), e);
        }
    }
}
