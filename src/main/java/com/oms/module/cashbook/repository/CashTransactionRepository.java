package com.oms.module.cashbook.repository;

import com.oms.module.cashbook.entity.CashTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CashTransactionRepository extends JpaRepository<CashTransaction, Long> {
    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM CashTransaction c WHERE c.transactionType = 'THU' AND MONTH(c.transactionDate) = :month AND YEAR(c.transactionDate) = :year")
    Double sumOtherIncomeByMonthAndYear(@Param("month") int month, @Param("year") int year);

    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM CashTransaction c WHERE c.transactionType = 'CHI' AND MONTH(c.transactionDate) = :month AND YEAR(c.transactionDate) = :year")
    Double sumOperatingExpensesByMonthAndYear(@Param("month") int month, @Param("year") int year);

    Optional<CashTransaction> findByVoucherCode(String voucherCode);

    boolean existsByVoucherCode(String voucherCode);
}