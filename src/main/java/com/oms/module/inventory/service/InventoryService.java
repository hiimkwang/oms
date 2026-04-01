package com.oms.module.inventory.service;

import com.oms.module.inventory.dto.InventoryDTO;
import com.oms.module.inventory.entity.Inventory;
import com.oms.module.inventory.repository.InventoryRepository;
import com.oms.module.product.entity.Product;
import com.oms.module.product.entity.ProductVariant;
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

    @Transactional(readOnly = true)
    public List<InventoryDTO> getInventoryList(Long branchId, String keyword, String stockStatus,
                                               Integer minStock, Integer maxStock, String dateRange) {

        // 1. Xử lý Logic Ngày Tháng (Từ tham số String sang LocalDateTime)
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;
        LocalDateTime now = LocalDateTime.now();

        if (dateRange != null && !dateRange.isEmpty()) {
            switch (dateRange) {
                case "today":
                    startDate = now.with(LocalTime.MIN);
                    endDate = now.with(LocalTime.MAX);
                    break;
                case "yesterday":
                    startDate = now.minusDays(1).with(LocalTime.MIN);
                    endDate = now.minusDays(1).with(LocalTime.MAX);
                    break;
                case "7days":
                    startDate = now.minusDays(7).with(LocalTime.MIN);
                    endDate = now.with(LocalTime.MAX);
                    break;
                case "30days":
                    startDate = now.minusDays(30).with(LocalTime.MIN);
                    endDate = now.with(LocalTime.MAX);
                    break;
                case "this_month":
                    startDate = now.withDayOfMonth(1).with(LocalTime.MIN);
                    endDate = now.with(LocalTime.MAX);
                    break;
                // Anh có thể thêm logic cho last_month tương tự như code cũ
            }
        }

        // Xử lý riêng logic stockStatus (in_stock / out_of_stock) kết hợp với min/max
        if ("in_stock".equals(stockStatus)) {
            minStock = (minStock == null || minStock <= 0) ? 1 : minStock;
        } else if ("out_of_stock".equals(stockStatus)) {
            maxStock = 0;
        }

        // 2. GỌI DB ĐÚNG 1 LẦN DUY NHẤT
        List<Object[]> rawResults = inventoryRepository.filterInventory(branchId, keyword, minStock, maxStock, startDate, endDate);
        List<InventoryDTO> result = new ArrayList<>();

        // 3. Map từ Object[] sang DTO
        for (Object[] row : rawResults) {
            Inventory inv = (Inventory) row[0];
            ProductVariant variant = (ProductVariant) row[1];
            Product product = (Product) row[2];

            String imgUrl = (variant.getImageUrl() != null && !variant.getImageUrl().isEmpty())
                    ? variant.getImageUrl()
                    : product.getImageUrl();

            InventoryDTO dto = InventoryDTO.builder()
                    .inventoryId(inv.getId())
                    .branchId(inv.getBranchId())
                    .stock(inv.getStock())
                    .availableStock(inv.getAvailableStock())
                    .variantId(variant.getId())
                    .inboundStock(inv.getInboundStock() != null ? inv.getInboundStock() : 0)
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

        return result;
    }
}