package com.oms.module.setting.config; // Nhớ đổi tên package cho khớp với project của ông nhé

import com.oms.module.setting.service.MasterDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

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
                masterDataService.createIfNotExist("CATEGORY", category);
            }
            System.out.println("✅ Đã tự động nạp dữ liệu Danh mục vào DB!");
        }

        // 2. Tự động nạp Hãng sản xuất (Brand)
        if (masterDataService.getValuesByType("BRAND").isEmpty()) {
            List<String> defaultBrands = List.of("Aula", "Leobog", "Akko", "Xinmeng", "Cherry", "Kailh", "Gateron", "FL Esports", "Khác");
            for (String brand : defaultBrands) {
                masterDataService.createIfNotExist("BRAND", brand);
            }
            System.out.println("✅ Đã tự động nạp dữ liệu Hãng sản xuất vào DB!");
        }

        // 3. Tự động nạp Đơn vị tính chuyên cho Mechkey
        if (masterDataService.getValuesByType("UNIT").isEmpty()) {
            List<String> defaultUnits = List.of("Chiếc", "Bộ", "Pack", "Lọ", "Tuýp", "Gram", "Sợi", "Tấm", "Cái");
            for (String unit : defaultUnits) {
                masterDataService.createIfNotExist("UNIT", unit);
            }
            System.out.println("✅ Đã tự động nạp dữ liệu Đơn vị tính vào DB!");
        }
    }
}