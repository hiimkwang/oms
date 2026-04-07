package com.oms.module.cashbook.service;

import com.oms.module.account.entity.User;
import com.oms.module.account.repository.UserRepository;
import com.oms.module.cashbook.dto.CashTransactionRequest;
import com.oms.module.cashbook.dto.CashbookSummary;
import com.oms.module.cashbook.entity.CashTransaction;
import com.oms.module.cashbook.repository.CashTransactionRepository;
import com.oms.module.customer.repository.CustomerRepository;
import com.oms.module.notification.entity.Notification;
import com.oms.module.notification.service.NotificationService;
import com.oms.module.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CashbookService {
    private final CashTransactionRepository cashRepo;
    private final CustomerRepository customerRepo;
    private final SupplierRepository supplierRepo;
    private final UserRepository userRepo;

    // 1. Inject NotificationService
    private final NotificationService notificationService;

    @Transactional
    public CashTransaction createTransaction(CashTransactionRequest request) {
        String finalCode = request.getCode();
        if (finalCode == null || finalCode.isEmpty()) {
            String prefix = (request.getType() == CashTransaction.TransactionType.RECEIPT) ? "PT" : "PC";
            finalCode = prefix + System.currentTimeMillis();
        }

        String targetName = "Khách vãng lai";
        if (request.getTargetId() != null) {
            if (request.getTargetGroup() == CashTransaction.TargetGroup.CUSTOMER) {
                targetName = customerRepo.findById(request.getTargetId()).map(c -> c.getFullName()).orElse(targetName);
            } else if (request.getTargetGroup() == CashTransaction.TargetGroup.SUPPLIER) {
                targetName = supplierRepo.findById(request.getTargetId()).map(s -> s.getName()).orElse(targetName);
            } else if (request.getTargetGroup() == CashTransaction.TargetGroup.EMPLOYEE) {
                targetName = userRepo.findById(request.getTargetId()).map(u -> u.getFullName()).orElse(targetName);
            }
        }

        String currentUser = getCurrentUserName();
        LocalDateTime finalDate = request.getTransactionDate();
        if (finalDate == null) {
            finalDate = LocalDateTime.now();
        } else if (finalDate.getHour() == 0 && finalDate.getMinute() == 0) {
            finalDate = finalDate.with(java.time.LocalTime.now());
        }

        CashTransaction transaction = CashTransaction.builder()
                .code(finalCode)
                .type(request.getType())
                .paymentMethod(request.getPaymentMethod())
                .targetGroup(request.getTargetGroup())
                .targetId(request.getTargetId())
                .targetName(targetName)
                .amount(request.getAmount())
                .reason(request.getReason())
                .description(request.getDescription())
                .branchId(request.getBranchId())
                .referenceCode(request.getReferenceCode())
                .transactionDate(finalDate)
                .creatorName(currentUser)
                .build();

        CashTransaction savedTransaction = cashRepo.save(transaction);

        // ---------------------------------------------------------
        // 2. BẮN THÔNG BÁO SAU KHI LƯU GIAO DỊCH THÀNH CÔNG
        // ---------------------------------------------------------
        try {
            boolean isReceipt = request.getType() == CashTransaction.TransactionType.RECEIPT;
            String typeName = isReceipt ? "Phiếu thu" : "Phiếu chi";
            String title = typeName + " mới: " + finalCode;

            // Format số tiền cho dễ đọc (VD: 1,500,000 đ)
            String formattedAmount = String.format("%,.0f đ", request.getAmount());
            String message = String.format("Đã ghi nhận %s với %s. Số tiền: %s",
                    typeName.toLowerCase(), targetName, formattedAmount);

            // Link chuyển hướng đến chi tiết (Tùy thuộc vào thiết kế Route thực tế của bạn)
            String link = isReceipt ? "/ui/cashbook/receipts/" + savedTransaction.getId()
                    : "/ui/cashbook/payments/" + savedTransaction.getId();

            notificationService.create(
                    title,
                    message,
                    Notification.NotificationType.PAYMENT, // Dùng loại PAYMENT sẽ hiển thị icon màu xanh lá (dựa theo code JS cũ)
                    link
            );
        } catch (Exception e) {
            // Log lỗi nếu cần thiết để không làm gián đoạn việc tạo phiếu thu/chi
            System.err.println("Lỗi khi gửi thông báo: " + e.getMessage());
        }
        // ---------------------------------------------------------

        return savedTransaction;
    }

    public CashbookSummary getSummary(LocalDateTime start, LocalDateTime end) {
        // 1. Quỹ đầu kỳ = Tổng thu trước start - Tổng chi trước start
        BigDecimal openingIn = cashRepo.sumAmountByTypeBefore(CashTransaction.TransactionType.RECEIPT, start);
        BigDecimal openingOut = cashRepo.sumAmountByTypeBefore(CashTransaction.TransactionType.PAYMENT, start);
        BigDecimal openingBalance = openingIn.subtract(openingOut);

        // 2. Tổng thu/chi trong kỳ
        BigDecimal totalIn = cashRepo.sumAmountByTypeBetween(CashTransaction.TransactionType.RECEIPT, start, end);
        BigDecimal totalOut = cashRepo.sumAmountByTypeBetween(CashTransaction.TransactionType.PAYMENT, start, end);

        // 3. Tồn theo loại quỹ
        BigDecimal cashIn = cashRepo.sumByMethodAndType(CashTransaction.PaymentMethod.CASH, CashTransaction.TransactionType.RECEIPT);
        BigDecimal cashOut = cashRepo.sumByMethodAndType(CashTransaction.PaymentMethod.CASH, CashTransaction.TransactionType.PAYMENT);

        BigDecimal bankIn = cashRepo.sumByMethodAndType(CashTransaction.PaymentMethod.BANK, CashTransaction.TransactionType.RECEIPT);
        BigDecimal bankOut = cashRepo.sumByMethodAndType(CashTransaction.PaymentMethod.BANK, CashTransaction.TransactionType.PAYMENT);

        return CashbookSummary.builder()
                .openingBalance(openingBalance)
                .totalIn(totalIn)
                .totalOut(totalOut)
                .closingBalance(openingBalance.add(totalIn).subtract(totalOut))
                .cashBalance(cashIn.subtract(cashOut))
                .bankBalance(bankIn.subtract(bankOut))
                .build();
    }

    public List<CashTransaction> getAllTransactions(LocalDateTime start, LocalDateTime end) {
        return cashRepo.findByTransactionDateBetweenOrderByTransactionDateDesc(start, end);
    }

    private String getCurrentUserName() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User) {
            return ((User) principal).getFullName();
        }
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public CashTransaction getById(Long id) {
        return cashRepo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu!"));
    }

    @Transactional
    public CashTransaction updateDetails(Long id, String description, String attachments) {
        CashTransaction tx = getById(id);
        tx.setDescription(description);
        tx.setAttachments(attachments);
        return cashRepo.save(tx);
    }

    @Transactional
    public void deleteTransaction(Long id) {
        cashRepo.deleteById(id);
    }

    public List<CashTransaction> filterTransactions(String keyword, Long branchId, String typeStr, LocalDateTime start, LocalDateTime end) {

        CashTransaction.TransactionType typeEnum = null;

        if (typeStr != null && !typeStr.trim().isEmpty()) {
            try {
                typeEnum = CashTransaction.TransactionType.valueOf(typeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                typeEnum = null;
            }
        }

        return cashRepo.searchAndFilter(keyword, branchId, typeEnum, start, end);
    }

    public CashbookSummary getSummary(LocalDateTime start, LocalDateTime end, Long branchId) {
        // 1. Lấy tổng thu/chi từ thuở sơ khai đến trước ngày 'start'
        BigDecimal totalInBefore = cashRepo.sumAmountBeforeDate(CashTransaction.TransactionType.RECEIPT, start, branchId);
        BigDecimal totalOutBefore = cashRepo.sumAmountBeforeDate(CashTransaction.TransactionType.PAYMENT, start, branchId);

        // => Tính ra Quỹ đầu kỳ
        BigDecimal openingBalance = totalInBefore.subtract(totalOutBefore);

        // 2. Lấy tổng thu/chi TRONG kỳ lọc (từ 'start' đến 'end')
        BigDecimal totalIn = cashRepo.sumAmountBetweenDates(CashTransaction.TransactionType.RECEIPT, start, end, branchId);
        BigDecimal totalOut = cashRepo.sumAmountBetweenDates(CashTransaction.TransactionType.PAYMENT, start, end, branchId);

        // 3. Tính Tồn quỹ cuối kỳ
        BigDecimal closingBalance = openingBalance.add(totalIn).subtract(totalOut);

        // Đóng gói gửi lên Controller
        return new CashbookSummary(openingBalance, totalIn, totalOut, closingBalance, new BigDecimal(0), new BigDecimal(0));
    }
}