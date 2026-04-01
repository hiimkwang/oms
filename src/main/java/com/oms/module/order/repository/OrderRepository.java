package com.oms.module.order.repository;

import com.oms.module.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE MONTH(o.createdAt) = :month AND YEAR(o.createdAt) = :year")
    Double sumTotalRevenueByMonthAndYear(@Param("month") int month, @Param("year") int year);

    Optional<Order> findByOrderCode(String orderCode);

    boolean existsByOrderCode(String orderCode);

    @Query("SELECT o FROM Order o WHERE LOWER(o.orderCode) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY o.createdAt DESC")
    List<Order> searchByKeyword(@Param("keyword") String keyword);
}