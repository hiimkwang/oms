package com.oms.module.gym.config;

import com.oms.module.gym.service.GymService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Khi khởi động: nạp dữ liệu mặc định cho module gym nếu các bảng còn trống
 * (database món ăn từ Excel, lịch tập PPL mẫu, cấu hình mặc định).
 */
@Component
@RequiredArgsConstructor
public class GymSeeder implements CommandLineRunner {

    private final GymService gymService;

    @Override
    public void run(String... args) {
        gymService.seedDefaults(false);
        // Bổ sung gợi ý khẩu phần cho DB đã seed từ phiên bản trước (idempotent)
        gymService.backfillFoodPortions();
    }
}
