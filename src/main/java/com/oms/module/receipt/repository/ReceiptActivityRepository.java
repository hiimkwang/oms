package com.oms.module.receipt.repository;

import com.oms.module.receipt.entity.ReceiptActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReceiptActivityRepository extends JpaRepository<ReceiptActivity, Long> {
    List<ReceiptActivity> findByReceiptIdOrderByCreatedAtDesc(Long receiptId);
}