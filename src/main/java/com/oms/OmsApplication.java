package com.oms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // Bật lịch tự động cho cảnh báo tồn kho thấp & backup database
@EnableCaching    // Bật cache (master data đọc mỗi lần render trang -> cache lại giảm truy vấn DB)
public class OmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(OmsApplication.class, args);
    }

    // Cache trong bộ nhớ (ConcurrentMap) — đủ cho master data ít đổi; khai báo tường minh
    // để không phụ thuộc auto-config (Spring Boot 4 không tự tạo CacheManager nếu thiếu provider).
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("masterDataByType");
    }

}
