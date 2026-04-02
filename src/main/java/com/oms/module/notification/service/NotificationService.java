package com.oms.module.notification.service;

import com.oms.module.notification.entity.Notification;
import com.oms.module.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepo;

    @Transactional
    public void create(String title, String message, Notification.NotificationType type, String link) {
        Notification notify = Notification.builder()
                .title(title)
                .message(message)
                .type(type)
                .link(link)
                .read(false)
                .build();
        notificationRepo.save(notify);
    }

    public List<Notification> getLatest() {
        return notificationRepo.findTop10ByOrderByCreatedAtDesc();
    }

    public long getUnreadCount() {
        return notificationRepo.countByReadFalse();
    }

    @Transactional
    public void markAsRead(Long id) {
        notificationRepo.findById(id).ifPresent(n -> n.setRead(true));
    }

    @Transactional
    public void markAllAsRead() {
        notificationRepo.findAll().forEach(n -> n.setRead(true));
    }
    public List<Notification> getAll() {
        return notificationRepo.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
    }
}