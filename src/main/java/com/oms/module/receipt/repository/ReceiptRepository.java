package com.oms.module.receipt.repository;

import com.oms.module.receipt.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    Optional<Receipt> findByCode(String code);
    boolean existsByCode(String code);
}