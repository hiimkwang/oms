package com.oms.module.receipt.repository;

import com.oms.module.receipt.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    Optional<Receipt> findByCode(String code);

    boolean existsByCode(String code);

    List<Receipt> findBySupplierCodeAndCreatedAtBetweenOrderByCreatedAtDesc(
            String code, LocalDateTime start, LocalDateTime end);

    List<Receipt> findBySupplierCodeOrderByCreatedAtDesc(String code);

    @Query("SELECT SUM(r.totalAmount - r.amountPaid) FROM Receipt r " +
            "WHERE r.supplier.code = :code AND r.status != 'CANCELLED'")
    BigDecimal getTotalDebtAllTime(@Param("code") String code);

    @Query("SELECT COUNT(r), SUM(r.totalAmount) FROM Receipt r " +
            "WHERE r.supplier.code = :code AND r.createdAt BETWEEN :start AND :end " +
            "AND r.status != 'CANCELLED'")
    Object[] getBasicStats(@Param("code") String code,
                           @Param("start") LocalDateTime start,
                           @Param("end") LocalDateTime end);

    @Query("SELECT SUM(r.totalAmount - COALESCE(r.amountPaid, 0)) FROM Receipt r " +
            "WHERE r.supplier.code = :code " +
            "AND r.paymentStatus IN ('UNPAID', 'PARTIAL') " +
            "AND r.status != 'CANCELLED' " +
            "AND r.createdAt BETWEEN :start AND :end")
    BigDecimal getTotalDebt(@Param("code") String code,
                            @Param("start") LocalDateTime start,
                            @Param("end") LocalDateTime end);
}