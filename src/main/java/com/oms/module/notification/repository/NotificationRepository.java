package com.oms.module.notification.repository;

import com.oms.module.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // Lấy 5-10 thông báo mới nhất để hiện trong dropdown chuông
    List<Notification> findTop10ByOrderByCreatedAtDesc();

    // Đếm số lượng chưa đọc để hiện Badge (con số đỏ)
    long countByReadFalse();
}