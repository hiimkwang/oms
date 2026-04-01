package com.oms.module.warranty.repository;

import com.oms.module.warranty.entity.WarrantyTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WarrantyTicketRepository extends JpaRepository<WarrantyTicket, Long> {

    @Query("SELECT w FROM WarrantyTicket w WHERE " +
            "(:status IS NULL OR w.status = :status) AND " +
            "(:type IS NULL OR w.type = :type) AND " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            " LOWER(w.ticketCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            " LOWER(w.customerName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            " LOWER(w.customerPhone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            " LOWER(w.serialNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            " LOWER(w.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) ) " +
            "ORDER BY w.receiveDate DESC")
    List<WarrantyTicket> filterTickets(
            @Param("keyword") String keyword,
            @Param("status") WarrantyTicket.TicketStatus status,
            @Param("type") WarrantyTicket.TicketType type);

    // Đếm số lượng máy đang sửa (RECEIVED, PROCESSING)
    @Query("SELECT COUNT(w) FROM WarrantyTicket w WHERE w.status IN ('RECEIVED', 'PROCESSING')")
    Long countProcessingWarranties();

    // Đếm số lượng máy mới nhận chưa ai xử lý (RECEIVED)
    Long countByStatus(WarrantyTicket.TicketStatus status);
}