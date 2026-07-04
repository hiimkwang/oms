package com.oms.module.returnorder.repository;
import com.oms.module.returnorder.entity.ReturnOrder;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReturnOrderRepository extends JpaRepository<ReturnOrder, Long> {
    Optional<ReturnOrder> findByReturnCode(String returnCode);

    // Đã tồn tại phiếu trả hàng (chưa bị từ chối) cho đơn gốc này hay chưa -> chống tạo trùng
    boolean existsByOriginalOrder_OrderCodeAndStatusNot(String orderCode, String status);

    // Tất cả phiếu trả của một đơn gốc (để cộng dồn số lượng đã trả, hỗ trợ trả một phần nhiều lần)
    java.util.List<ReturnOrder> findByOriginalOrder_OrderCode(String orderCode);

    // Lọc + tìm kiếm server-side, JOIN FETCH đơn gốc để tránh N+1 (thay cho findAll() + lọc trong bộ nhớ)
    @Query("SELECT r FROM ReturnOrder r JOIN FETCH r.originalOrder o WHERE " +
            "(:status IS NULL OR :status = '' OR r.status = :status) AND " +
            "(:kw IS NULL OR :kw = '' OR LOWER(r.returnCode) LIKE LOWER(CONCAT('%', :kw, '%')) " +
            " OR LOWER(o.orderCode) LIKE LOWER(CONCAT('%', :kw, '%'))) " +
            "ORDER BY r.createdAt DESC")
    java.util.List<ReturnOrder> search(@Param("status") String status, @Param("kw") String kw);

    // Khóa ghi dòng phiếu trả: đảm bảo hoàn tiền / nhập kho lại chỉ chạy 1 lần (idempotent)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM ReturnOrder r WHERE r.id = :id")
    Optional<ReturnOrder> findByIdForUpdate(@Param("id") Long id);
}