package com.oms.module.setting.controller;

import com.oms.module.setting.entity.MasterData;
import com.oms.module.setting.service.MasterDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/settings/master-data")
@RequiredArgsConstructor
public class MasterDataController {

    private final MasterDataService masterDataService;

    // API thêm nhanh Master Data (Dùng chung cho Hãng, Danh mục, ĐVT...)
    @PostMapping("/quick-add")
    public ResponseEntity<MasterData> quickAdd(@RequestBody Map<String, String> payload) {
        String type = payload.get("type"); // Ví dụ: BRAND, CATEGORY
        String value = payload.get("value"); // Ví dụ: Xinmeng, Phụ kiện custom
        String label = payload.get("label"); // Có thể có hoặc không từ Frontend

        if (type == null || value == null || value.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // Xử lý thông minh:
        // Nếu Frontend chỉ gửi 1 ô input "value", ta lấy nó làm Label (hiển thị)
        // và chuyển sang viết hoa không dấu/có dấu làm Value (để lưu DB chuẩn)
        if (label == null || label.trim().isEmpty()) {
            label = value.trim();
        }

        // Value hệ thống luôn để in hoa cho đồng nhất
        String formattedValue = value.trim().toUpperCase();

        // Gọi service thêm mới với 3 tham số (Type, Value, Label)
        MasterData newData = masterDataService.createIfNotExist(type, formattedValue, label);

        return ResponseEntity.ok(newData);
    }

    // API lưu Cấu hình hệ thống chung
    @PostMapping("/settings")
    public ResponseEntity<?> saveSettings(@RequestBody Map<String, String> payload) {
        masterDataService.saveSystemConfigs(payload);
        return ResponseEntity.ok(Map.of("message", "Lưu cấu hình thành công!"));
    }
}