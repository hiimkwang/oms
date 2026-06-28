package com.oms.module.setting.service;

import com.oms.module.notification.entity.Notification;
import com.oms.module.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Tự động sao lưu database theo lịch (mặc định 1h sáng mỗi ngày).
 * Lịch cấu hình tại application.properties: app.backup.cron
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BackupScheduler {

    private final BackupService backupService;
    private final NotificationService notificationService;

    @Scheduled(cron = "${app.backup.cron:0 0 1 * * *}")
    public void scheduledBackup() {
        if (!backupService.isEnabled()) {
            return;
        }
        BackupService.BackupResult result = backupService.performBackup();
        if (!result.success()) {
            // Báo cho admin biết để xử lý kịp thời (dữ liệu tài chính không được mất)
            notificationService.create(
                    "Sao lưu database THẤT BẠI",
                    "Hệ thống không sao lưu được dữ liệu: " + result.message(),
                    Notification.NotificationType.ERROR,
                    "/ui/settings/backup");
        }
    }
}
