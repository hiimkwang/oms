package com.oms.module.order.repository;

import com.oms.module.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE MONTH(o.orderDate) = :month AND YEAR(o.orderDate) = :year")
    Double sumTotalRevenueByMonthAndYear(@Param("month") int month, @Param("year") int year);

    Optional<Order> findByOrderCode(String orderCode);

    boolean existsByOrderCode(String orderCode);
}