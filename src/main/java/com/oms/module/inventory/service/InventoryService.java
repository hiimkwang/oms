package com.oms.module.inventory.service;

import com.oms.module.inventory.dto.InventoryDTO;
import com.oms.module.inventory.entity.Inventory;
import com.oms.module.inventory.repository.InventoryRepository;
import com.oms.module.product.entity.Product;
import com.oms.module.product.entity.ProductVariant;
import com.oms.module.product.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductVariantRepository variantRepository;

    @Transactional(readOnly = true)
    public List<InventoryDTO> getInventoryList(Long branchId, String keyword, String stockStatus,
                                               Integer minStock, Integer maxStock, String dateRange) {

        // 1. Lấy toàn bộ tồn kho của Chi nhánh này
        List<Inventory> inventories;
        if (branchId != null) {
            inventories = inventoryRepository.findByBranchId(branchId);
        } else {
            inventories = inventoryRepository.findAll();
        }

        List<InventoryDTO> result = new ArrayList<>();

        // 2. Map với thông tin Sản phẩm & Lọc dữ liệu
        for (Inventory inv : inventories) {
            ProductVariant variant = variantRepository.findById(inv.getVariantId()).orElse(null);
            if (variant == null) continue;

            Product product = variant.getProduct();

            // --- LỌC TỪ KHÓA (SKU hoặc Tên SP) ---
            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = keyword.toLowerCase().trim();
                boolean matchName = product.getName() != null && product.getName().toLowerCase().contains(kw);
                boolean matchSku = variant.getSku() != null && variant.getSku().toLowerCase().contains(kw);
                if (!matchName && !matchSku) continue;
            }

            // --- LỌC TRẠNG THÁI KHO ---
            if ("in_stock".equals(stockStatus) && inv.getStock() <= 0) continue;
            if ("out_of_stock".equals(stockStatus) && inv.getStock() > 0) continue;

            // --- LỌC MIN / MAX TỒN KHO ---
            if (minStock != null && inv.getStock() < minStock) continue;
            if (maxStock != null && inv.getStock() > maxStock) continue;

            // --- LỌC THEO NGÀY TẠO ---
            if (dateRange != null && !dateRange.isEmpty() && product.getCreatedAt() != null) {
                LocalDateTime start = null;
                LocalDateTime end = null;
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime productDate = product.getCreatedAt();

                switch (dateRange) {
                    case "today":
                        start = now.with(LocalTime.MIN);
                        end = now.with(LocalTime.MAX);
                        break;
                    case "yesterday":
                        start = now.minusDays(1).with(LocalTime.MIN);
                        end = now.minusDays(1).with(LocalTime.MAX);
                        break;
                    case "7days":
                        start = now.minusDays(7).with(LocalTime.MIN);
                        end = now.with(LocalTime.MAX);
                        break;
                    case "30days":
                        start = now.minusDays(30).with(LocalTime.MIN);
                        end = now.with(LocalTime.MAX);
                        break;
                    case "this_month":
                        start = now.withDayOfMonth(1).with(LocalTime.MIN);
                        end = now.with(LocalTime.MAX);
                        break;
                    case "last_month":
                        start = now.minusMonths(1).withDayOfMonth(1).with(LocalTime.MIN);
                        end = now.withDayOfMonth(1).minusDays(1).with(LocalTime.MAX);
                        break;
                }

                if (start != null && end != null) {
                    if (productDate.isBefore(start) || productDate.isAfter(end)) {
                        continue; // Bỏ qua nếu không nằm trong khoảng ngày
                    }
                }
            }

            // 3. Build DTO trả về
            String imgUrl = (variant.getImageUrl() != null && !variant.getImageUrl().isEmpty())
                    ? variant.getImageUrl()
                    : product.getImageUrl();

            InventoryDTO dto = InventoryDTO.builder()
                    .inventoryId(inv.getId())
                    .branchId(inv.getBranchId())
                    .stock(inv.getStock())
                    .availableStock(inv.getAvailableStock())
                    .variantId(variant.getId())
                    .productName(product.getName())
                    .variantName(variant.getVariantName())
                    .sku(variant.getSku())
                    .unit(product.getUnit())
                    .imageUrl(imgUrl)
                    .costPrice(variant.getCostPrice())
                    .price(variant.getPrice())
                    .createdAt(product.getCreatedAt())
                    .build();

            result.add(dto);
        }

        // Tùy chọn: Sắp xếp theo ngày tạo mới nhất (hoặc tồn kho tùy anh)
        result.sort((d1, d2) -> {
            if (d1.getCreatedAt() == null || d2.getCreatedAt() == null) return 0;
            return d2.getCreatedAt().compareTo(d1.getCreatedAt());
        });

        return result;
    }
}