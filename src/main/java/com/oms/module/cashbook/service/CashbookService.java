package com.oms.module.cashbook.service;

import com.oms.module.account.entity.User;
import com.oms.module.account.repository.UserRepository;
import com.oms.module.cashbook.dto.CapitalSummary;
import com.oms.module.cashbook.dto.CashTransactionRequest;
import com.oms.module.cashbook.dto.CashbookSummary;
import com.oms.module.cashbook.entity.CashTransaction;
import com.oms.module.cashbook.repository.CashTransactionRepository;
import com.oms.module.customer.repository.CustomerRepository;
import com.oms.module.inventory.repository.InventoryRepository;
import com.oms.module.notification.entity.Notification;
import com.oms.module.notification.service.NotificationService;
import com.oms.module.order.repository.OrderRepository;
import com.oms.module.receipt.repository.ReceiptRepository;
import com.oms.module.supplier.repository.SupplierRepository;
import com.oms.module.setting.repository.SalesChannelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CashbookService {
    private final CashTransactionRepository cashRepo;
    private final CustomerRepository customerRepo;
    private final SupplierRepository supplierRepo;
    private final UserRepository userRepo;
    private final NotificationService notificationService;
    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;
    private final ReceiptRepository receiptRepository;
    private final SalesChannelRepository salesChannelRepo;

    // Lý do (reason) chuẩn cho các giao dịch vốn — phải khớp với option trong form phiếu thu/chi
    public static final String REASON_CAPITAL_IN = "Nhận vốn góp";
    public static final String REASON_CAPITAL_OUT = "Rút vốn";

    // Lý do chuẩn cho dòng tiền BÁN HÀNG (đối xứng với "Trả nợ nhà cung cấp" ở phiếu nhập).
    // Mỗi lần khách trả tiền đơn hàng -> tự sinh phiếu THU với lý do này để Sổ quỹ phản ánh đủ tiền vào.
    public static final String REASON_SALE_IN = "Thu tiền bán hàng";
    // Khi giảm tiền đã thu (sửa đơn xuống/hủy đơn đã thu) -> phiếu CHI điều chỉnh để cân sổ.
    public static final String REASON_SALE_ADJUST = "Điều chỉnh tiền bán hàng";

    @Transactional
    public CashTransaction createTransaction(CashTransactionRequest request) {
        return createTransaction(request, true);
    }

    /**
     * @param notify true: bắn thông báo (chuông) cho phiếu thu/chi nhập tay.
     *               false: phiếu sinh TỰ ĐỘNG (vd thu tiền từng đơn hàng) -> KHÔNG rung chuông để tránh spam,
     *               nhưng vẫn ghi đầy đủ vào Sổ quỹ để cân tiền.
     */
    @Transactional
    public CashTransaction createTransaction(CashTransactionRequest request, boolean notify) {
        String finalCode = request.getCode();
        if (finalCode == null || finalCode.isEmpty()) {
            String prefix = (request.getType() == CashTransaction.TransactionType.RECEIPT) ? "PT" : "PC";
            // Thêm hậu tố ngẫu nhiên để tránh trùng mã khi tạo nhiều giao dịch trong cùng mili-giây
            finalCode = prefix + System.currentTimeMillis() + java.util.UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        }

        String targetName = "Khách vãng lai";
        if (request.getTargetId() != null) {
            if (request.getTargetGroup() == CashTransaction.TargetGroup.CUSTOMER) {
                targetName = customerRepo.findById(request.getTargetId()).map(c -> c.getFullName()).orElse(targetName);
            } else if (request.getTargetGroup() == CashTransaction.TargetGroup.SUPPLIER) {
                targetName = supplierRepo.findById(request.getTargetId()).map(s -> s.getName()).orElse(targetName);
            } else if (request.getTargetGroup() == CashTransaction.TargetGroup.EMPLOYEE) {
                targetName = userRepo.findById(request.getTargetId()).map(u -> u.getFullName()).orElse(targetName);
            } else if (request.getTargetGroup() == CashTransaction.TargetGroup.ECOMMERCE) {
                targetName = salesChannelRepo.findById(request.getTargetId()).map(ch -> ch.getName()).orElse(targetName);
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

        if (notify) try {
            boolean isReceipt = request.getType() == CashTransaction.TransactionType.RECEIPT;
            String typeName = isReceipt ? "Phiếu thu" : "Phiếu chi";
            String title = typeName + " mới: " + finalCode;

            String formattedAmount = String.format("%,.0f đ", request.getAmount());
            String message = String.format("Đã ghi nhận %s với %s. Số tiền: %s",
                    typeName.toLowerCase(), targetName, formattedAmount);

            String link = "/ui/cashbook/detail/" + savedTransaction.getId();

            notificationService.create(
                    title,
                    message,
                    Notification.NotificationType.PAYMENT,
                    link
            );
        } catch (Exception e) {
            log.error("Lỗi khi gửi thông báo: {}", e.getMessage(), e);
        }

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
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return "Hệ thống";
        Object principal = auth.getPrincipal();
        if (principal instanceof User) {
            return ((User) principal).getFullName();
        }
        return auth.getName();
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

    public List<CashTransaction> filterTransactions(String keyword, Long branchId, String typeStr, String reason, LocalDateTime start, LocalDateTime end) {

        CashTransaction.TransactionType typeEnum = null;

        if (typeStr != null && !typeStr.trim().isEmpty()) {
            try {
                typeEnum = CashTransaction.TransactionType.valueOf(typeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                typeEnum = null;
            }
        }

        return cashRepo.searchAndFilter(keyword, branchId, typeEnum, reason, start, end);
    }

    public List<String> getDistinctReasons() {
        return cashRepo.findDistinctReasons();
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

        // 4. Số dư Tiền mặt / Ngân hàng hiện tại (toàn bộ thời gian) theo chi nhánh đang lọc
        BigDecimal cashIn = cashRepo.sumByMethodAndTypeAndBranch(CashTransaction.PaymentMethod.CASH, CashTransaction.TransactionType.RECEIPT, branchId);
        BigDecimal cashOut = cashRepo.sumByMethodAndTypeAndBranch(CashTransaction.PaymentMethod.CASH, CashTransaction.TransactionType.PAYMENT, branchId);
        BigDecimal bankIn = cashRepo.sumByMethodAndTypeAndBranch(CashTransaction.PaymentMethod.BANK, CashTransaction.TransactionType.RECEIPT, branchId);
        BigDecimal bankOut = cashRepo.sumByMethodAndTypeAndBranch(CashTransaction.PaymentMethod.BANK, CashTransaction.TransactionType.PAYMENT, branchId);

        // Đóng gói gửi lên Controller
        return new CashbookSummary(openingBalance, totalIn, totalOut, closingBalance,
                cashIn.subtract(cashOut), bankIn.subtract(bankOut));
    }

    // === QUẢN LÝ VỐN ===

    private BigDecimal nz(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    public CapitalSummary getCapitalSummary() {
        BigDecimal contributed = nz(cashRepo.sumByTypeAndReason(CashTransaction.TransactionType.RECEIPT, REASON_CAPITAL_IN));
        BigDecimal withdrawn = nz(cashRepo.sumByTypeAndReason(CashTransaction.TransactionType.PAYMENT, REASON_CAPITAL_OUT));
        BigDecimal netCapital = contributed.subtract(withdrawn);

        BigDecimal inventoryValue = nz(inventoryRepository.sumTotalInventoryValue());

        // Số dư tiền mặt / ngân hàng toàn thời gian (toàn hệ thống)
        BigDecimal cashIn = nz(cashRepo.sumByMethodAndType(CashTransaction.PaymentMethod.CASH, CashTransaction.TransactionType.RECEIPT));
        BigDecimal cashOut = nz(cashRepo.sumByMethodAndType(CashTransaction.PaymentMethod.CASH, CashTransaction.TransactionType.PAYMENT));
        BigDecimal bankIn = nz(cashRepo.sumByMethodAndType(CashTransaction.PaymentMethod.BANK, CashTransaction.TransactionType.RECEIPT));
        BigDecimal bankOut = nz(cashRepo.sumByMethodAndType(CashTransaction.PaymentMethod.BANK, CashTransaction.TransactionType.PAYMENT));

        BigDecimal cashBalance = cashIn.subtract(cashOut);
        BigDecimal bankBalance = bankIn.subtract(bankOut);
        BigDecimal fundBalance = cashBalance.add(bankBalance);
        BigDecimal currentAssets = fundBalance.add(inventoryValue);

        // Tổng tiền đã thu từ bán hàng (để hiển thị riêng trên trang Vốn)
        BigDecimal salesReceived = nz(cashRepo.sumByTypeAndReason(CashTransaction.TransactionType.RECEIPT, REASON_SALE_IN));

        BigDecimal receivables = nz(orderRepository.sumCustomerReceivables());
        BigDecimal payables = nz(receiptRepository.sumSupplierPayables());

        // Vốn thực = tiền quỹ + tồn kho + khách còn nợ mình - mình còn nợ NCC
        BigDecimal netWorth = currentAssets.add(receivables).subtract(payables);
        BigDecimal growth = netWorth.subtract(netCapital);

        return CapitalSummary.builder()
                .totalContributed(contributed)
                .totalWithdrawn(withdrawn)
                .netCapital(netCapital)
                .inventoryValue(inventoryValue)
                .cashBalance(cashBalance)
                .bankBalance(bankBalance)
                .fundBalance(fundBalance)
                .salesReceived(salesReceived)
                .currentAssets(currentAssets)
                .receivables(receivables)
                .payables(payables)
                .netWorth(netWorth)
                .growth(growth)
                .build();
    }

    public List<CashTransaction> getCapitalTransactions() {
        return cashRepo.findByReasonInOrderByTransactionDateDesc(List.of(REASON_CAPITAL_IN, REASON_CAPITAL_OUT));
    }

    /**
     * Ghi phiếu THU tiền bán hàng cho một đơn (dùng khi đánh dấu đã thanh toán ngoài luồng OrderService,
     * ví dụ đối soát sàn). Không rung chuông. amount là phần tiền MỚI thu thêm (delta).
     */
    /** Tổng tiền bán hàng đã ghi nhận (ròng) cho một đơn — dùng cho đối soát idempotent. */
    public BigDecimal getRecordedSaleCash(String orderCode) {
        return nz(cashRepo.sumNetSaleCashByReference(orderCode));
    }

    @Transactional
    public void recordSaleReceipt(Long customerId, String orderCode, Long branchId, String orderPaymentMethod, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) return;

        CashTransaction.PaymentMethod pm = CashTransaction.PaymentMethod.CASH;
        if (orderPaymentMethod != null && (orderPaymentMethod.equalsIgnoreCase("TRANSFER") || orderPaymentMethod.equalsIgnoreCase("BANK"))) {
            pm = CashTransaction.PaymentMethod.BANK;
        }

        CashTransactionRequest req = new CashTransactionRequest();
        req.setType(CashTransaction.TransactionType.RECEIPT);
        req.setPaymentMethod(pm);
        req.setTargetGroup(CashTransaction.TargetGroup.CUSTOMER);
        req.setTargetId(customerId);
        req.setAmount(amount);
        req.setReason(REASON_SALE_IN);
        req.setDescription("Thu tiền đơn hàng " + orderCode + " (đối soát)");
        req.setBranchId(branchId);
        req.setReferenceCode(orderCode);
        req.setTransactionDate(LocalDateTime.now());
        createTransaction(req, false);
    }

    /**
     * Đảo ngược toàn bộ phiếu CHI gắn với một mã tham chiếu (vd khi HỦY phiếu nhập đã trả tiền NCC).
     * Mỗi phiếu chi được bù bằng 1 phiếu THU cùng hình thức (tiền mặt/ngân hàng) và số tiền,
     * giữ đúng số dư từng quỹ. Idempotent: mã phiếu đảo cố định theo id nên không tạo trùng.
     */
    @Transactional
    public void reversePaymentsByReference(String referenceCode) {
        if (referenceCode == null || referenceCode.isBlank()) return;
        String currentUser = getCurrentUserName();
        List<CashTransaction> payments = cashRepo.findByReferenceCodeAndType(
                referenceCode, CashTransaction.TransactionType.PAYMENT);
        for (CashTransaction p : payments) {
            String reversalCode = "PT-REV-" + p.getId();
            if (cashRepo.existsByCode(reversalCode)) continue; // đã đảo rồi -> bỏ qua

            CashTransaction reversal = CashTransaction.builder()
                    .code(reversalCode)
                    .type(CashTransaction.TransactionType.RECEIPT)
                    .paymentMethod(p.getPaymentMethod())
                    .targetGroup(p.getTargetGroup())
                    .targetId(p.getTargetId())
                    .targetName(p.getTargetName())
                    .amount(p.getAmount())
                    .reason("Hoàn tiền hủy phiếu nhập")
                    .description("Đảo phiếu chi " + p.getCode() + " do hủy phiếu nhập " + referenceCode)
                    .referenceCode(referenceCode)
                    .transactionDate(LocalDateTime.now())
                    .creatorName(currentUser)
                    .build();
            cashRepo.save(reversal);
        }
    }

    // === BÁO CÁO THU/CHI THEO LOẠI ===
    public com.oms.module.cashbook.dto.CashFlowReport getCashFlowByCategory(
            LocalDateTime start, LocalDateTime end, Long branchId, String keyword) {

        List<CashTransaction> txns = cashRepo.findInRange(start, end, branchId);
        String kw = keyword == null ? "" : keyword.trim().toLowerCase();

        // Gộp theo lý do, tách Thu/Chi
        java.util.Map<String, BigDecimal> inSum = new java.util.LinkedHashMap<>();
        java.util.Map<String, Long> inCnt = new java.util.LinkedHashMap<>();
        java.util.Map<String, BigDecimal> outSum = new java.util.LinkedHashMap<>();
        java.util.Map<String, Long> outCnt = new java.util.LinkedHashMap<>();

        BigDecimal totalIn = BigDecimal.ZERO, totalOut = BigDecimal.ZERO;
        BigDecimal matchedIn = BigDecimal.ZERO, matchedOut = BigDecimal.ZERO;
        List<CashTransaction> matched = new java.util.ArrayList<>();

        for (CashTransaction t : txns) {
            BigDecimal amt = nz(t.getAmount());
            String reason = (t.getReason() == null || t.getReason().isBlank()) ? "Khác" : t.getReason();
            boolean isReceipt = t.getType() == CashTransaction.TransactionType.RECEIPT;

            if (isReceipt) {
                inSum.merge(reason, amt, BigDecimal::add);
                inCnt.merge(reason, 1L, Long::sum);
                totalIn = totalIn.add(amt);
            } else {
                outSum.merge(reason, amt, BigDecimal::add);
                outCnt.merge(reason, 1L, Long::sum);
                totalOut = totalOut.add(amt);
            }

            // Lọc theo từ khóa: tìm trong lý do + mô tả + tên đối tượng + mã tham chiếu
            if (!kw.isEmpty()) {
                String hay = ((t.getReason() == null ? "" : t.getReason()) + " " +
                        (t.getDescription() == null ? "" : t.getDescription()) + " " +
                        (t.getTargetName() == null ? "" : t.getTargetName()) + " " +
                        (t.getReferenceCode() == null ? "" : t.getReferenceCode())).toLowerCase();
                if (hay.contains(kw)) {
                    matched.add(t);
                    if (isReceipt) matchedIn = matchedIn.add(amt);
                    else matchedOut = matchedOut.add(amt);
                }
            }
        }

        List<com.oms.module.cashbook.dto.CashFlowReport.Line> receiptLines = toLines(inSum, inCnt, totalIn);
        List<com.oms.module.cashbook.dto.CashFlowReport.Line> paymentLines = toLines(outSum, outCnt, totalOut);

        return com.oms.module.cashbook.dto.CashFlowReport.builder()
                .receiptLines(receiptLines)
                .paymentLines(paymentLines)
                .totalIn(totalIn)
                .totalOut(totalOut)
                .net(totalIn.subtract(totalOut))
                .keyword(keyword)
                .matched(matched)
                .matchedIn(matchedIn)
                .matchedOut(matchedOut)
                .build();
    }

    private List<com.oms.module.cashbook.dto.CashFlowReport.Line> toLines(
            java.util.Map<String, BigDecimal> sums, java.util.Map<String, Long> counts, BigDecimal total) {
        return sums.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .map(e -> {
                    double pct = total.signum() > 0
                            ? e.getValue().multiply(new BigDecimal("100")).divide(total, 1, java.math.RoundingMode.HALF_UP).doubleValue()
                            : 0d;
                    return new com.oms.module.cashbook.dto.CashFlowReport.Line(
                            e.getKey(), e.getValue(), counts.getOrDefault(e.getKey(), 0L), pct);
                })
                .collect(java.util.stream.Collectors.toList());
    }
}