package com.oms.module.inventory.repository;

import com.oms.module.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    @Query("SELECT i, v, p FROM Inventory i " +
            "JOIN ProductVariant v ON i.variantId = v.id " +
            "JOIN Product p ON v.product.id = p.id " +
            "WHERE (:branchId IS NULL OR i.branchId = :branchId) " +
            "AND (:keyword IS NULL OR :keyword = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(v.sku) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:minStock IS NULL OR i.stock >= :minStock) " +
            "AND (:maxStock IS NULL OR i.stock <= :maxStock) " +
            "AND (:startDate IS NULL OR p.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR p.createdAt <= :endDate) " +
            "ORDER BY p.createdAt DESC")
    List<Object[]> filterInventory(
            @Param("branchId") Long branchId,
            @Param("keyword") String keyword,
            @Param("minStock") Integer minStock,
            @Param("maxStock") Integer maxStock,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // Lấy số tồn kho vật lý dựa vào mã SKU và Chi nhánh
    @Query("SELECT i.stock FROM Inventory i, ProductVariant v WHERE i.variantId = v.id AND i.branchId = :branchId AND v.sku = :sku")
    Integer getStockByBranchAndSku(@Param("branchId") Long branchId, @Param("sku") String sku);
    // Tính tổng giá trị tồn kho toàn hệ thống
    @Query("SELECT COALESCE(SUM(i.stock * v.costPrice), 0) FROM Inventory i JOIN ProductVariant v ON i.variantId = v.id")
    BigDecimal sumTotalInventoryValue();
}