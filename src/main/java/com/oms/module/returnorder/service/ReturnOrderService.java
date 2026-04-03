package com.oms.module.returnorder.service;

import com.oms.module.account.entity.User;
import com.oms.module.cashbook.repository.CashTransactionRepository;
import com.oms.module.inventory.repository.InventoryRepository;
import com.oms.module.order.entity.Order;
import com.oms.module.order.repository.OrderRepository;
import com.oms.module.product.repository.ProductVariantRepository;
import com.oms.module.returnorder.dto.ReturnOrderRequest;
import com.oms.module.returnorder.entity.ReturnActivity;
import com.oms.module.returnorder.entity.ReturnOrder;
import com.oms.module.returnorder.entity.ReturnOrderDetail;
import com.oms.module.returnorder.repository.ReturnOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReturnOrderService {

    private final ReturnOrderRepository returnRepo;
    private final OrderRepository orderRepo;
    // KHAI BÁO THÊM CÁC REPOSITORY NÀY
    private final CashTransactionRepository cashRepo;
    private final ProductVariantRepository variantRepo;
    private final InventoryRepository inventoryRepo;

    private String getCurrentUserName() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof User) return ((User) principal).getFullName();
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "Hệ thống";
        }
    }

    @Transactional
    public ReturnOrder createReturnOrder(ReturnOrderRequest request) {
        Order originalOrder = orderRepo.findByOrderCode(request.getOriginalOrderCode())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn gốc!"));

        String user = getCurrentUserName();
        String returnCode = "TH" + System.currentTimeMillis();

        ReturnOrder returnOrder = ReturnOrder.builder()
                .returnCode(returnCode)
                .originalOrder(originalOrder)
                .reason(request.getReason())
                .note(request.getNote())
                .returnFee(request.getReturnFee() != null ? request.getReturnFee() : BigDecimal.ZERO)
                .refundStatus("UNPAID")
                .restockStatus("PENDING")
                .status("PROCESSING")
                .createdBy(user)
                .build();

        BigDecimal totalRefund = BigDecimal.ZERO;
        List<ReturnOrderDetail> details = new ArrayList<>();

        for (ReturnOrderRequest.ReturnItemRequest item : request.getDetails()) {
            BigDecimal lineTotal = item.getUnitPrice().multiply(new BigDecimal(item.getQuantity()));
            totalRefund = totalRefund.add(lineTotal);

            details.add(ReturnOrderDetail.builder()
                    .returnOrder(returnOrder)
                    .sku(item.getSku())
                    .productName(item.getProductName())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .refundAmount(lineTotal)
                    .build());
        }

        // Khách được hoàn lại = Tổng tiền hàng trả - Phí trả hàng (shop thu thêm)
        returnOrder.setTotalRefundAmount(totalRefund.subtract(returnOrder.getReturnFee()));
        returnOrder.setDetails(details);

        // Ghi Log
        ReturnActivity act = ReturnActivity.builder()
                .returnOrder(returnOrder)
                .action("Tạo phiếu trả hàng")
                .description("Lý do: " + request.getReason())
                .createdBy(user)
                .build();
        returnOrder.getActivities().add(act);

        return returnRepo.save(returnOrder);
    }

    public ReturnOrder getByCode(String code) {
        return returnRepo.findByReturnCode(code)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu trả hàng: " + code));
    }

    public List<ReturnOrder> getAllReturns(String keyword, String status) {
        List<ReturnOrder> all = returnRepo.findAll();

        return all.stream()
                .filter(r -> status == null || status.isEmpty() || status.equals(r.getStatus()))
                .filter(r -> keyword == null || keyword.isEmpty() ||
                        r.getReturnCode().toLowerCase().contains(keyword.toLowerCase()) ||
                        r.getOriginalOrder().getOrderCode().toLowerCase().contains(keyword.toLowerCase()))
                .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
                .toList();
    }

    // 1. HÀM XỬ LÝ HOÀN TIỀN
    @Transactional
    public void processRefund(Long returnOrderId, String method) {
        ReturnOrder returnOrder = returnRepo.findById(returnOrderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu trả hàng"));

        if ("REFUNDED".equals(returnOrder.getRefundStatus())) {
            throw new RuntimeException("Phiếu này đã được hoàn tiền!");
        }

        // Tự động tạo phiếu chi trong Sổ quỹ
        com.oms.module.cashbook.entity.CashTransaction payment = com.oms.module.cashbook.entity.CashTransaction.builder()
                .code("PC-HT-" + System.currentTimeMillis())
                .type(com.oms.module.cashbook.entity.CashTransaction.TransactionType.PAYMENT)
                .paymentMethod(com.oms.module.cashbook.entity.CashTransaction.PaymentMethod.valueOf(method))
                .targetGroup(com.oms.module.cashbook.entity.CashTransaction.TargetGroup.CUSTOMER)
                .targetId(returnOrder.getOriginalOrder().getCustomer() != null ? returnOrder.getOriginalOrder().getCustomer().getId() : null)
                .targetName(returnOrder.getOriginalOrder().getCustomer() != null ? returnOrder.getOriginalOrder().getCustomer().getFullName() : "Khách trả hàng")
                .amount(returnOrder.getTotalRefundAmount())
                .reason("Hoàn tiền cho khách") // Bạn có thể cấu hình reason này khớp với reason tính Lợi nhuận nếu muốn
                .description("Hoàn tiền phiếu trả hàng " + returnOrder.getReturnCode() + " (Đơn gốc: " + returnOrder.getOriginalOrder().getOrderCode() + ")")
                .transactionDate(java.time.LocalDateTime.now())
                .creatorName(getCurrentUserName())
                .build();
        cashRepo.save(payment);

        returnOrder.setRefundStatus("REFUNDED");
        checkAndCompleteReturn(returnOrder);

        logActivity(returnOrder, "Xác nhận hoàn tiền", "Đã hoàn " + returnOrder.getTotalRefundAmount() + "đ qua " + method);
        returnRepo.save(returnOrder);
    }

    // 2. HÀM XỬ LÝ NHẬP LẠI KHO
    @Transactional
    public void processRestock(Long returnOrderId, Long branchId) {
        ReturnOrder returnOrder = returnRepo.findById(returnOrderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu trả hàng"));

        if ("RESTOCKED".equals(returnOrder.getRestockStatus())) {
            throw new RuntimeException("Phiếu này đã được nhập kho!");
        }

        // Cộng lại tồn kho cho từng sản phẩm
        for (com.oms.module.returnorder.entity.ReturnOrderDetail detail : returnOrder.getDetails()) {
            com.oms.module.product.entity.ProductVariant variant = variantRepo.findBySku(detail.getSku()).orElse(null);
            if (variant != null) {
                // Tăng tồn kho tổng
                variant.setStockQuantity((variant.getStockQuantity() != null ? variant.getStockQuantity() : 0) + detail.getQuantity());
                variantRepo.save(variant);

                // Tăng tồn kho chi tiết tại chi nhánh
                com.oms.module.inventory.entity.Inventory inv = inventoryRepo.findByVariantIdAndBranchId(variant.getId(), branchId)
                        .orElse(com.oms.module.inventory.entity.Inventory.builder()
                                .variantId(variant.getId()).branchId(branchId).stock(0).availableStock(0).build());
                inv.setStock(inv.getStock() + detail.getQuantity());
                inv.setAvailableStock(inv.getAvailableStock() + detail.getQuantity());
                inventoryRepo.save(inv);
            }
        }

        returnOrder.setRestockStatus("RESTOCKED");
        checkAndCompleteReturn(returnOrder);

        logActivity(returnOrder, "Nhận hàng vào kho", "Đã cất lại hàng vào kho chi nhánh ID: " + branchId);
        returnRepo.save(returnOrder);
    }

    // 3. KIỂM TRA XONG CẢ 2 BƯỚC THÌ CHỐT PHIẾU
    private void checkAndCompleteReturn(ReturnOrder returnOrder) {
        if ("REFUNDED".equals(returnOrder.getRefundStatus()) && "RESTOCKED".equals(returnOrder.getRestockStatus())) {
            returnOrder.setStatus("COMPLETED");
            logActivity(returnOrder, "Hoàn tất phiếu trả hàng", "Đã xử lý xong các bước hoàn tiền và nhập kho.");
        }
    }

    @Transactional
    public void deleteBulk(List<Long> ids) {
        returnRepo.deleteAllById(ids);
    }

    // 4. HÀM GHI LOG
    private void logActivity(ReturnOrder returnOrder, String action, String description) {
        com.oms.module.returnorder.entity.ReturnActivity act = com.oms.module.returnorder.entity.ReturnActivity.builder()
                .returnOrder(returnOrder)
                .action(action)
                .description(description)
                .createdBy(getCurrentUserName())
                .build();
        if (returnOrder.getActivities() == null) returnOrder.setActivities(new java.util.ArrayList<>());
        returnOrder.getActivities().add(act);
    }
}