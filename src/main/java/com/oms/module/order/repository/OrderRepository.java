package com.oms.module.order.repository;

import com.oms.module.order.entity.Order;
import com.oms.module.order.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

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

    // 1. Thống kê Doanh thu theo Ngày (Dùng hàm DATE của MySQL)
    @Query("SELECT FUNCTION('DATE', o.createdAt), SUM(o.totalAmount) FROM Order o WHERE o.status NOT IN ('Đã hủy', 'Khởi tạo') AND o.createdAt BETWEEN :start AND :end GROUP BY FUNCTION('DATE', o.createdAt) ORDER BY FUNCTION('DATE', o.createdAt) ASC")
    List<Object[]> findRevenueByDate(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 2. Thống kê Số lượng đơn hàng theo Ngày
    @Query("SELECT FUNCTION('DATE', o.createdAt), COUNT(o) FROM Order o WHERE o.status NOT IN ('Đã hủy', 'Khởi tạo') AND o.createdAt BETWEEN :start AND :end GROUP BY FUNCTION('DATE', o.createdAt) ORDER BY FUNCTION('DATE', o.createdAt) ASC")
    List<Object[]> findOrderCountByDate(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 3. Top sản phẩm bán chạy nhất
    @Query("SELECT d.productName, SUM(d.quantity) FROM OrderDetail d WHERE d.order.status NOT IN ('Đã hủy', 'Khởi tạo') AND d.order.createdAt BETWEEN :start AND :end GROUP BY d.productName ORDER BY SUM(d.quantity) DESC")
    List<Object[]> findTopSellingProducts(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);

    // 4. Doanh thu theo Chi nhánh
    @Query("SELECT COALESCE(b.name, 'Chưa gán'), SUM(o.totalAmount) FROM Order o LEFT JOIN Branch b ON o.branchId = b.id WHERE o.status NOT IN ('Đã hủy', 'Khởi tạo') AND o.createdAt BETWEEN :start AND :end GROUP BY b.name")
    List<Object[]> findRevenueByBranch(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 1. Tính Lợi nhuận gộp theo Từng Ngày (Để vẽ biểu đồ Tỷ suất lợi nhuận)
    @Query("SELECT FUNCTION('DATE', o.createdAt), SUM(d.totalPrice - (d.quantity * v.costPrice)) " +
            "FROM OrderDetail d JOIN d.order o JOIN ProductVariant v ON d.sku = v.sku " +
            "WHERE o.status NOT IN ('Đã hủy', 'Khởi tạo') AND o.createdAt BETWEEN :start AND :end " +
            "GROUP BY FUNCTION('DATE', o.createdAt) ORDER BY FUNCTION('DATE', o.createdAt) ASC")
    List<Object[]> findProfitByDate(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 2. Tính Lợi nhuận gộp theo Kênh bán
    @Query("SELECT COALESCE(o.salesChannelCode, 'Tại quầy'), SUM(d.totalPrice - (d.quantity * v.costPrice)) " +
            "FROM OrderDetail d JOIN d.order o JOIN ProductVariant v ON d.sku = v.sku " +
            "WHERE o.status NOT IN ('Đã hủy', 'Khởi tạo') AND o.createdAt BETWEEN :start AND :end " +
            "GROUP BY o.salesChannelCode")
    List<Object[]> findProfitByChannel(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 3. Tính Lợi nhuận gộp theo Chi nhánh
    @Query("SELECT COALESCE(b.name, 'Hệ thống'), SUM(d.totalPrice - (d.quantity * v.costPrice)) " +
            "FROM OrderDetail d JOIN d.order o LEFT JOIN Branch b ON o.branchId = b.id JOIN ProductVariant v ON d.sku = v.sku " +
            "WHERE o.status NOT IN ('Đã hủy', 'Khởi tạo') AND o.createdAt BETWEEN :start AND :end " +
            "GROUP BY b.name")
    List<Object[]> findProfitByBranch(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // =====================================
    // TAB KHÁCH HÀNG
    // =====================================
    @Query("SELECT c.fullName, SUM(o.totalAmount) FROM Order o JOIN o.customer c WHERE o.status NOT IN ('Đã hủy', 'Khởi tạo') AND o.createdAt BETWEEN :start AND :end GROUP BY c.fullName ORDER BY SUM(o.totalAmount) DESC")
    List<Object[]> findTopCustomers(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);

    // =====================================
    // TAB: PHÂN TÍCH THEO KÊNH BÁN (Tự động 100%)
    // =====================================

    @Query("SELECT DISTINCT o.salesChannelCode FROM Order o WHERE o.salesChannelCode IS NOT NULL")
    List<String> findAllDistinctChannels();

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.salesChannelCode IN :channels AND o.status NOT IN ('Đã hủy', 'Khởi tạo') AND o.createdAt BETWEEN :start AND :end")
    BigDecimal sumChannelTabNetRevenue(@Param("channels") List<String> channels, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(d.quantity * v.costPrice), 0) FROM OrderDetail d JOIN d.order o JOIN ProductVariant v ON d.sku = v.sku WHERE o.salesChannelCode IN :channels AND o.status NOT IN ('Đã hủy', 'Khởi tạo') AND o.createdAt BETWEEN :start AND :end")
    BigDecimal sumChannelTabCOGS(@Param("channels") List<String> channels, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.salesChannelCode IN :channels AND o.status = :status AND o.createdAt BETWEEN :start AND :end")
    Long countChannelTabOrdersByStatus(@Param("channels") List<String> channels, @Param("status") String status, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.salesChannelCode IN :channels AND o.status NOT IN ('Đã hủy', 'Khởi tạo') AND o.createdAt BETWEEN :start AND :end")
    Long countTotalChannelTabOrders(@Param("channels") List<String> channels, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(o.salesChannelCode, 'Khác'), SUM(o.totalAmount) FROM Order o WHERE o.salesChannelCode IN :channels AND o.status NOT IN ('Đã hủy', 'Khởi tạo') AND o.createdAt BETWEEN :start AND :end GROUP BY o.salesChannelCode")
    List<Object[]> findChannelTabRevenueByChannel(@Param("channels") List<String> channels, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(b.name, 'Chưa gán'), SUM(o.totalAmount) FROM Order o LEFT JOIN Branch b ON o.branchId = b.id WHERE o.salesChannelCode IN :channels AND o.status NOT IN ('Đã hủy', 'Khởi tạo') AND o.createdAt BETWEEN :start AND :end GROUP BY b.name")
    List<Object[]> findChannelTabRevenueByBranch(@Param("channels") List<String> channels, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT d.productName, SUM(d.quantity) FROM OrderDetail d JOIN d.order o WHERE o.salesChannelCode IN :channels AND o.status NOT IN ('Đã hủy', 'Khởi tạo') AND o.createdAt BETWEEN :start AND :end GROUP BY d.productName ORDER BY SUM(d.quantity) DESC")
    List<Object[]> findChannelTabTopProducts(@Param("channels") List<String> channels, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);

    @Query("SELECT d.productName, SUM(d.quantity) FROM OrderDetail d JOIN d.order o WHERE o.salesChannelCode IN :channels AND o.status IN ('Đã hủy', 'Trả hàng') AND o.createdAt BETWEEN :start AND :end GROUP BY d.productName ORDER BY SUM(d.quantity) DESC")
    List<Object[]> findChannelTabTopReturnedProducts(@Param("channels") List<String> channels, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);

    // Trả về Object[]: [0]=imageUrl, [1]=FullProductName (nối biến thể), [2]=sku, [3]=totalQty, [4]=totalRevenue
    @Query("SELECT v.imageUrl, " +
            "CASE WHEN v.variantName IS NOT NULL AND v.variantName <> 'Mặc định' " +
            "     THEN CONCAT(d.productName, ' - ', v.variantName) " +
            "     ELSE d.productName END, " +
            "d.sku, SUM(d.quantity), SUM(d.quantity * d.unitPrice) " +
            "FROM OrderDetail d JOIN d.order o LEFT JOIN ProductVariant v ON d.sku = v.sku " +
            "WHERE o.status NOT IN ('Đã hủy', 'Khởi tạo') AND o.createdAt BETWEEN :start AND :end " +
            "GROUP BY v.imageUrl, d.productName, v.variantName, d.sku " +
            "ORDER BY SUM(d.quantity) DESC")
    List<Object[]> findDashboardTopProducts(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);
}