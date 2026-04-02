package com.oms.module.notification.controller;

import com.oms.module.notification.entity.Notification;
import com.oms.module.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class DashboardNotificationController {

    private final NotificationService notificationService;

    @GetMapping("/latest")
    public List<Notification> getLatest() {
        return notificationService.getLatest();
    }

    @GetMapping("/unread-count")
    public Map<String, Long> getUnreadCount() {
        Map<String, Long> res = new HashMap<>();
        res.put("count", notificationService.getUnreadCount());
        return res;
    }

    @PostMapping("/mark-read/{id}")
    public void read(@PathVariable Long id) {
        notificationService.markAsRead(id);
    }

    @PostMapping("/mark-all-read")
    public void markAllRead() {
        notificationService.markAllAsRead();
    }
}