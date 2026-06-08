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

    // Khóa ghi dòng phiếu trả: đảm bảo hoàn tiền / nhập kho lại chỉ chạy 1 lần (idempotent)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM ReturnOrder r WHERE r.id = :id")
    Optional<ReturnOrder> findByIdForUpdate(@Param("id") Long id);
}