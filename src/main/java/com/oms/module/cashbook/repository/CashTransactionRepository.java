package com.oms.module.cashbook.repository;

import com.oms.module.cashbook.entity.CashTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CashTransactionRepository extends JpaRepository<CashTransaction, Long> {
    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM CashTransaction c WHERE c.type = 'RECEIPT' AND c.reason = 'Thu nhập khác' AND MONTH(c.transactionDate) = :month AND YEAR(c.transactionDate) = :year")
    Double sumOtherIncomeByMonthAndYear(@Param("month") int month, @Param("year") int year);

    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM CashTransaction c WHERE c.type = 'PAYMENT' AND (c.reason LIKE 'Chi phí %' OR c.reason = 'Chi lương nhân viên') AND MONTH(c.transactionDate) = :month AND YEAR(c.transactionDate) = :year")
    Double sumOperatingExpensesByMonthAndYear(@Param("month") int month, @Param("year") int year);

    // Phiên bản BigDecimal (dùng cho báo cáo lợi nhuận chính xác về tiền tệ)
    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM CashTransaction c WHERE c.type = 'RECEIPT' AND c.reason = 'Thu nhập khác' AND MONTH(c.transactionDate) = :month AND YEAR(c.transactionDate) = :year")
    BigDecimal sumOtherIncomeByMonthAndYearBD(@Param("month") int month, @Param("year") int year);

    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM CashTransaction c WHERE c.type = 'PAYMENT' AND (c.reason LIKE 'Chi phí %' OR c.reason = 'Chi lương nhân viên') AND MONTH(c.transactionDate) = :month AND YEAR(c.transactionDate) = :year")
    BigDecimal sumOperatingExpensesByMonthAndYearBD(@Param("month") int month, @Param("year") int year);

    boolean existsByCode(String code);

    @Query("SELECT COALESCE(SUM(CASE WHEN t.type = 'RECEIPT' THEN t.amount ELSE 0 END), 0) - " +
            "       COALESCE(SUM(CASE WHEN t.type = 'PAYMENT' THEN t.amount ELSE 0 END), 0) " +
            "FROM CashTransaction t " +
            "WHERE t.transactionDate < :startDate")
    BigDecimal calculateOpeningBalance(@Param("startDate") LocalDateTime startDate);

    // 1. Tính tổng số tiền của một LOẠI (Thu/Chi) TRƯỚC một thời điểm (Để tính Đầu Kỳ)
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM CashTransaction t " +
            "WHERE t.type = :type AND t.transactionDate < :date")
    BigDecimal sumAmountByTypeBefore(@Param("type") CashTransaction.TransactionType type,
                                     @Param("date") LocalDateTime date);

    // 2. Tính tổng số tiền của một LOẠI trong KHOẢNG thời gian (Để tính Tổng Thu/Chi trong kỳ)
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM CashTransaction t " +
            "WHERE t.type = :type " +
            "AND t.transactionDate BETWEEN :start AND :end")
    BigDecimal sumAmountByTypeBetween(
            @Param("type") CashTransaction.TransactionType type,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // 3. Tính tổng theo PHƯƠNG THỨC và LOẠI (Để tính số dư Tiền mặt / Tiền gửi hiện tại)
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM CashTransaction t " +
            "WHERE t.paymentMethod = :method AND t.type = :type")
    BigDecimal sumByMethodAndType(@Param("method") CashTransaction.PaymentMethod method,
                                  @Param("type") CashTransaction.TransactionType type);

    // 3b. Như trên nhưng lọc theo chi nhánh (dùng cho số dư Tiền mặt/Ngân hàng theo chi nhánh)
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM CashTransaction t " +
            "WHERE t.paymentMethod = :method AND t.type = :type AND (:branchId IS NULL OR t.branchId = :branchId)")
    BigDecimal sumByMethodAndTypeAndBranch(@Param("method") CashTransaction.PaymentMethod method,
                                           @Param("type") CashTransaction.TransactionType type,
                                           @Param("branchId") Long branchId);

    List<CashTransaction> findByTransactionDateBetweenOrderByTransactionDateDesc(
            LocalDateTime start, LocalDateTime end);

    @Query("SELECT t FROM CashTransaction t WHERE " +
            "(t.transactionDate BETWEEN :start AND :end) AND " +
            "(:branchId IS NULL OR t.branchId = :branchId) AND " +
            "(:type IS NULL OR t.type = :type) AND " +
            "(:reason IS NULL OR :reason = '' OR t.reason = :reason) AND " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            " LOWER(t.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            " LOWER(t.targetName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            " LOWER(t.reason) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            " LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            " LOWER(t.referenceCode) LIKE LOWER(CONCAT('%', :keyword, '%')) ) " +
            "ORDER BY t.transactionDate DESC")
    List<CashTransaction> searchAndFilter(
            @Param("keyword") String keyword,
            @Param("branchId") Long branchId,
            @Param("type") CashTransaction.TransactionType type,
            @Param("reason") String reason,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // Danh sách các LÝ DO đang có trong sổ quỹ (để đổ vào dropdown lọc)
    @Query("SELECT DISTINCT t.reason FROM CashTransaction t WHERE t.reason IS NOT NULL AND t.reason <> '' ORDER BY t.reason")
    List<String> findDistinctReasons();

    // 1. TÍNH TỔNG THU/CHI TỪ XƯA ĐẾN TRƯỚC NGÀY BẮT ĐẦU (Dùng cho Quỹ đầu kỳ)
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM CashTransaction t WHERE t.type = :type AND t.transactionDate < :startDate AND (:branchId IS NULL OR t.branchId = :branchId)")
    BigDecimal sumAmountBeforeDate(@Param("type") CashTransaction.TransactionType type,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("branchId") Long branchId);

    // 2. TÍNH TỔNG THU/CHI TRONG KHOẢNG THỜI GIAN LỌC (Dùng cho Tổng thu/Tổng chi)
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM CashTransaction t WHERE t.type = :type AND (t.transactionDate BETWEEN :startDate AND :endDate) AND (:branchId IS NULL OR t.branchId = :branchId)")
    BigDecimal sumAmountBetweenDates(@Param("type") CashTransaction.TransactionType type,
                                     @Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate,
                                     @Param("branchId") Long branchId);

    // Tính tổng chi phí vận hành (Tổng Phiếu Chi) trong tháng
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM CashTransaction t WHERE t.type = 'PAYMENT' AND (t.reason LIKE 'Chi phí %' OR t.reason = 'Chi lương nhân viên') AND t.transactionDate BETWEEN :start AND :end")
    BigDecimal sumOperatingExpenses(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);


    // Lấy TỔNG THU NHẬP KHÁC theo khoảng thời gian
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM CashTransaction t WHERE t.type = 'RECEIPT' AND t.reason = 'Thu nhập khác' AND t.transactionDate BETWEEN :start AND :end")
    BigDecimal sumOtherIncomeBetweenDates(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Lấy TỔNG CHI PHÍ VẬN HÀNH theo khoảng thời gian
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM CashTransaction t WHERE t.type = 'PAYMENT' AND (t.reason LIKE 'Chi phí %' OR t.reason = 'Chi lương nhân viên') AND t.transactionDate BETWEEN :start AND :end")
    BigDecimal sumOperatingExpensesBetweenDates(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // === QUẢN LÝ VỐN ===
    // Tổng tiền theo LOẠI giao dịch và LÝ DO (dùng cho vốn góp / vốn rút)
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM CashTransaction t WHERE t.type = :type AND t.reason = :reason")
    BigDecimal sumByTypeAndReason(@Param("type") CashTransaction.TransactionType type, @Param("reason") String reason);

    // Sổ ghi nhận các giao dịch vốn (góp/rút), mới nhất lên đầu
    List<CashTransaction> findByReasonInOrderByTransactionDateDesc(List<String> reasons);

    // Lấy các phiếu theo mã tham chiếu (vd mã phiếu nhập) và loại — dùng để ĐẢO phiếu khi hủy
    List<CashTransaction> findByReferenceCodeAndType(String referenceCode, CashTransaction.TransactionType type);

    // Lấy toàn bộ giao dịch trong khoảng thời gian (+ lọc chi nhánh) để gộp/báo cáo theo loại
    @Query("SELECT t FROM CashTransaction t WHERE (t.transactionDate BETWEEN :start AND :end) " +
            "AND (:branchId IS NULL OR t.branchId = :branchId) ORDER BY t.transactionDate DESC")
    List<CashTransaction> findInRange(@Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end,
                                     @Param("branchId") Long branchId);
}