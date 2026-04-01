package com.oms.module.order.repository;

import com.oms.module.order.entity.Order;
import com.oms.module.order.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    // 1. Doanh thu thuần (Bỏ qua đơn Hủy)
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status NOT IN ('Hủy', 'Đã hủy', 'CANCELED', 'Khởi tạo') AND o.createdAt BETWEEN :start AND :end")
    BigDecimal sumNetRevenue(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    // 2. Tổng số lượng hàng thực bán (Chọc vào OrderDetail, bỏ qua đơn Hủy)
    @Query("SELECT COALESCE(SUM(d.quantity), 0) FROM OrderDetail d WHERE d.order.status NOT IN ('Hủy', 'Đã hủy', 'CANCELED') AND d.order.createdAt BETWEEN :start AND :end")
    Long sumTotalItemsSold(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 3. Đếm tổng đơn hàng trong kỳ
    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :start AND :end")
    Long countTotalOrders(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 4. Đếm đơn hàng theo trạng thái chung (Dùng cho Chưa giao, Đang giao, Hủy)
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status AND o.createdAt BETWEEN :start AND :end")
    Long countOrdersByStatus(@Param("status") String status, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 5. Đếm đơn Chưa thanh toán (Bỏ qua các đơn đã Hủy)
    @Query("SELECT COUNT(o) FROM Order o WHERE (o.paymentStatus = 'UNPAID' OR o.paymentStatus = 'Chưa thanh toán' OR o.paymentStatus IS NULL) AND o.status NOT IN ('Hủy', 'Đã hủy') AND o.createdAt BETWEEN :start AND :end")
    Long countUnpaidOrders(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    // Tính TỔNG GIÁ VỐN HÀNG BÁN (Chỉ tính các đơn đã chốt, không tính đơn Hủy/Khởi tạo)
    @Query("SELECT COALESCE(SUM(d.quantity * v.costPrice), 0) FROM OrderDetail d JOIN ProductVariant v ON d.sku = v.sku WHERE d.order.status NOT IN ('Đã hủy', 'Khởi tạo') AND d.order.createdAt BETWEEN :start AND :end")
    BigDecimal sumTotalCOGS(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    List<Order> findByCustomer_CodeOrderByCreatedAtDesc(String customerCode);
    List<Order> findTop10ByCustomer_CodeOrderByCreatedAtDesc(String customerCode);
    List<Order> findTop5ByOrderByCreatedAtDesc();
    @Query("SELECT COALESCE(o.salesChannelCode, 'Tại quầy'), COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :start AND :end GROUP BY o.salesChannelCode")
    List<Object[]> countOrdersByChannel(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}