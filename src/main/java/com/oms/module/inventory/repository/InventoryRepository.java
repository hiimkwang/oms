package com.oms.module.inventory.repository;

import com.oms.module.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    // 1. Tìm chính xác số tồn kho của 1 sản phẩm tại 1 chi nhánh cụ thể (Dùng khi Tạo đơn/Trừ kho)
    Optional<Inventory> findByVariantIdAndBranchId(Long variantId, Long branchId);

    // 2. Xem 1 sản phẩm đang nằm rải rác ở những chi nhánh nào (Dùng cho Modal Kiểm tra tồn kho)
    List<Inventory> findByVariantId(Long variantId);

    // 3. Xem toàn bộ tồn kho của 1 chi nhánh (Dùng cho Báo cáo Chi nhánh)
    List<Inventory> findByBranchId(Long branchId);
}