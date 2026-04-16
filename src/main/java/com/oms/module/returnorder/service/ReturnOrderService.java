package com.oms.module.returnorder.service;

import com.oms.module.account.entity.User;
import com.oms.module.cashbook.entity.CashTransaction;
import com.oms.module.cashbook.repository.CashTransactionRepository;
import com.oms.module.inventory.repository.InventoryRepository;
import com.oms.module.notification.entity.Notification;
import com.oms.module.notification.service.NotificationService;
import com.oms.module.order.entity.Order;
import com.oms.module.order.entity.OrderActivity;
import com.oms.module.order.repository.OrderRepository;
import com.oms.module.product.repository.ProductVariantRepository;
import com.oms.module.returnorder.dto.ReturnOrderRequest;
import com.oms.module.returnorder.entity.ReturnActivity;
import com.oms.module.returnorder.entity.ReturnOrder;
import com.oms.module.returnorder.entity.ReturnOrderDetail;
import com.oms.module.returnorder.repository.ReturnOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.oms.constant.CommonConstants.OrderStatusConstant.RETURNED;
import static com.oms.constant.CommonConstants.PaymentStatusConstant.*;
import static com.oms.constant.CommonConstants.ReturnStatusConstant.*;
import static com.oms.constant.CommonConstants.ReturnStatusConstant.PENDING;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReturnOrderService {

    private final ReturnOrderRepository returnRepo;
    private final OrderRepository orderRepo;
    private final CashTransactionRepository cashRepo;
    private final ProductVariantRepository variantRepo;
    private final InventoryRepository inventoryRepo;
    private final NotificationService notificationService;

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
        Order originalOrder = orderRepo.findByOrderCode(request.getOriginalOrderCode()).orElseThrow(() -> new RuntimeException("Không tìm thấy đơn gốc!"));

        String user = getCurrentUserName();
        String returnCode = "TH" + System.currentTimeMillis();

        originalOrder.setStatus(RETURNED);

        OrderActivity orderLog = OrderActivity.builder().order(originalOrder).action("Chuyển trạng thái").description("Đơn hàng chuyển sang trạng thái TRẢ HÀNG do có phiếu yêu cầu: " + returnCode).createdBy(user).createdAt(java.time.LocalDateTime.now()).build();

        if (originalOrder.getActivities() == null) {
            originalOrder.setActivities(new java.util.ArrayList<>());
        }
        originalOrder.getActivities().add(orderLog);
        orderRepo.save(originalOrder);

        try {
            notificationService.create("Đơn hàng bị trả lại: " + originalOrder.getOrderCode(), "Đơn hàng đã được chuyển sang trạng thái TRẢ HÀNG.", Notification.NotificationType.ORDER, "/ui/orders/detail/" + originalOrder.getOrderCode());
        } catch (Exception e) {
            log.error("Lỗi Noti đơn hàng: {}", e.getMessage());
        }

        ReturnOrder returnOrder = ReturnOrder.builder().returnCode(returnCode).originalOrder(originalOrder).reason(request.getReason()).note(request.getNote()).returnFee(request.getReturnFee() != null ? request.getReturnFee() : BigDecimal.ZERO) // Phí khách chịu
                .shopReturnFee(request.getShopReturnFee() != null ? request.getShopReturnFee() : BigDecimal.ZERO).refundStatus(UNPAID).restockStatus(RESTOCK_PENDING).status(PENDING).createdBy(user).build();

        BigDecimal totalRefund = BigDecimal.ZERO;
        List<ReturnOrderDetail> details = new ArrayList<>();

        for (ReturnOrderRequest.ReturnItemRequest item : request.getDetails()) {
            BigDecimal lineTotal = item.getUnitPrice().multiply(new BigDecimal(item.getQuantity()));
            totalRefund = totalRefund.add(lineTotal);

            details.add(ReturnOrderDetail.builder().returnOrder(returnOrder).sku(item.getSku()).productName(item.getProductName()).quantity(item.getQuantity()).unitPrice(item.getUnitPrice()).refundAmount(lineTotal).build());
        }

        returnOrder.setTotalRefundAmount(totalRefund.subtract(returnOrder.getReturnFee()));
        returnOrder.setDetails(details);

        ReturnActivity act = ReturnActivity.builder().returnOrder(returnOrder).action("Tạo phiếu trả hàng").description("Lý do: " + request.getReason()).createdBy(user).build();
        returnOrder.getActivities().add(act);

        ReturnOrder savedReturnOrder = returnRepo.save(returnOrder);

        // ---------------------------------------------------------
        // 2. BẮN THÔNG BÁO TẠO PHIẾU TRẢ HÀNG MỚI
        // ---------------------------------------------------------
        try {
            String title = "Yêu cầu trả hàng: " + savedReturnOrder.getReturnCode();
            String message = "Đơn gốc: " + originalOrder.getOrderCode() + " | Lý do: " + request.getReason();
            String link = "/ui/returns/" + savedReturnOrder.getReturnCode();

            notificationService.create(title, message, Notification.NotificationType.WARNING, link);
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi thông báo trả hàng: " + e.getMessage());
        }

        return savedReturnOrder;
    }

    public ReturnOrder getByCode(String code) {
        return returnRepo.findByReturnCode(code).orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu trả hàng: " + code));
    }

    public List<ReturnOrder> getAllReturns(String keyword, String status) {
        List<ReturnOrder> all = returnRepo.findAll();

        return all.stream().filter(r -> status == null || status.isEmpty() || status.equals(r.getStatus())).filter(r -> keyword == null || keyword.isEmpty() || r.getReturnCode().toLowerCase().contains(keyword.toLowerCase()) || r.getOriginalOrder().getOrderCode().toLowerCase().contains(keyword.toLowerCase())).sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt())).toList();
    }

    @Transactional
    public void processRefund(Long returnOrderId, String method) {
        ReturnOrder returnOrder = returnRepo.findById(returnOrderId).orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu trả hàng"));

        if (REFUNDED.equals(returnOrder.getRefundStatus())) {
            throw new RuntimeException("Phiếu này đã được hoàn tiền!");
        }

        // 1. TẠO PHIẾU CHI: HOÀN TIỀN CHO KHÁCH (Như cũ)
        if (returnOrder.getTotalRefundAmount().compareTo(BigDecimal.ZERO) > 0) {
            com.oms.module.cashbook.entity.CashTransaction refundPayment = com.oms.module.cashbook.entity.CashTransaction.builder().code("PC-HT-" + System.currentTimeMillis()).type(com.oms.module.cashbook.entity.CashTransaction.TransactionType.PAYMENT).paymentMethod(com.oms.module.cashbook.entity.CashTransaction.PaymentMethod.valueOf(method)).targetGroup(com.oms.module.cashbook.entity.CashTransaction.TargetGroup.CUSTOMER).targetId(returnOrder.getOriginalOrder().getCustomer() != null ? returnOrder.getOriginalOrder().getCustomer().getId() : null).targetName(returnOrder.getOriginalOrder().getCustomer() != null ? returnOrder.getOriginalOrder().getCustomer().getFullName() : "Khách trả hàng").amount(returnOrder.getTotalRefundAmount()).reason("Hoàn tiền cho khách").description("Hoàn tiền phiếu trả hàng " + returnOrder.getReturnCode() + " (Đơn gốc: " + returnOrder.getOriginalOrder().getOrderCode() + ")").transactionDate(java.time.LocalDateTime.now()).creatorName(getCurrentUserName()).build();
            cashRepo.save(refundPayment);
        }

        // 2. TẠO PHIẾU CHI (MỚI): CHI PHÍ PHÁT SINH SHOP TỰ CHỊU
        if (returnOrder.getShopReturnFee() != null && returnOrder.getShopReturnFee().compareTo(BigDecimal.ZERO) > 0) {
            com.oms.module.cashbook.entity.CashTransaction shopFeePayment = com.oms.module.cashbook.entity.CashTransaction.builder().code("PC-PH-" + (System.currentTimeMillis() + 1)) // Cộng 1ms để tránh trùng mã code
                    .type(com.oms.module.cashbook.entity.CashTransaction.TransactionType.PAYMENT).paymentMethod(com.oms.module.cashbook.entity.CashTransaction.PaymentMethod.valueOf(method)).targetGroup(com.oms.module.cashbook.entity.CashTransaction.TargetGroup.OTHER) // Ghi nhận là chi phí Khác/Nội bộ
                    .targetName("Chi phí vận hành (Trả hàng)").amount(returnOrder.getShopReturnFee()).reason("Phí phát sinh hàng hoàn").description("Chi phí Shop tự chịu cho phiếu trả hàng " + returnOrder.getReturnCode() + " (Đơn gốc: " + returnOrder.getOriginalOrder().getOrderCode() + ")").transactionDate(java.time.LocalDateTime.now()).creatorName(getCurrentUserName()).build();
            cashRepo.save(shopFeePayment);
        }

        returnOrder.setRefundStatus(REFUNDED);
        checkAndCompleteReturn(returnOrder);

        // Lưu log có ghi chú thêm về khoản phí shop chịu
        String logDesc = "Đã hoàn " + returnOrder.getTotalRefundAmount() + "đ qua " + method;
        if (returnOrder.getShopReturnFee() != null && returnOrder.getShopReturnFee().compareTo(BigDecimal.ZERO) > 0) {
            logDesc += " (Ghi nhận thêm " + returnOrder.getShopReturnFee() + "đ chi phí Shop chịu vào Sổ quỹ)";
        }

        logActivity(returnOrder, "Xác nhận xuất quỹ hoàn tiền", logDesc);
        returnRepo.save(returnOrder);
    }

    @Transactional
    public void processRestock(Long returnOrderId, Long branchId) {
        ReturnOrder returnOrder = returnRepo.findById(returnOrderId).orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu trả hàng"));

        if (RESTOCK_RESTOCKED.equals(returnOrder.getRestockStatus())) {
            throw new RuntimeException("Phiếu này đã được nhập kho!");
        }

        for (com.oms.module.returnorder.entity.ReturnOrderDetail detail : returnOrder.getDetails()) {
            com.oms.module.product.entity.ProductVariant variant = variantRepo.findBySku(detail.getSku()).orElse(null);
            if (variant != null) {
                variant.setStockQuantity((variant.getStockQuantity() != null ? variant.getStockQuantity() : 0) + detail.getQuantity());
                variantRepo.save(variant);

                com.oms.module.inventory.entity.Inventory inv = inventoryRepo.findByVariantIdAndBranchId(variant.getId(), branchId).orElse(com.oms.module.inventory.entity.Inventory.builder().variantId(variant.getId()).branchId(branchId).stock(0).availableStock(0).build());
                inv.setStock(inv.getStock() + detail.getQuantity());
                inv.setAvailableStock(inv.getAvailableStock() + detail.getQuantity());
                inventoryRepo.save(inv);
            }
        }

        returnOrder.setRestockStatus(RESTOCK_RESTOCKED);
        checkAndCompleteReturn(returnOrder);

        logActivity(returnOrder, "Nhận hàng vào kho", "Đã cất lại hàng vào kho chi nhánh ID: " + branchId);
        returnRepo.save(returnOrder);
    }

    private void checkAndCompleteReturn(ReturnOrder returnOrder) {
        if (REFUNDED.equals(returnOrder.getRefundStatus()) && RESTOCK_RESTOCKED.equals(returnOrder.getRestockStatus())) {
            returnOrder.setStatus(COMPLETED);
            logActivity(returnOrder, "Hoàn tất phiếu trả hàng", "Đã xử lý xong các bước hoàn tiền và nhập kho.");

            try {
                notificationService.create("Hoàn tất trả hàng: " + returnOrder.getReturnCode(), "Đã xử lý xong hoàn tiền và nhập lại kho cho đơn gốc " + returnOrder.getOriginalOrder().getOrderCode(), Notification.NotificationType.RETURN, "/ui/returns/" + returnOrder.getReturnCode());
            } catch (Exception e) {
                System.err.println("Lỗi khi gửi thông báo hoàn tất trả hàng: " + e.getMessage());
            }

        }
    }

    @Transactional
    public void deleteBulk(List<Long> ids) {
        returnRepo.deleteAllById(ids);
    }

    private void logActivity(ReturnOrder returnOrder, String action, String description) {
        com.oms.module.returnorder.entity.ReturnActivity act = com.oms.module.returnorder.entity.ReturnActivity.builder().returnOrder(returnOrder).action(action).description(description).createdBy(getCurrentUserName()).build();
        if (returnOrder.getActivities() == null) returnOrder.setActivities(new java.util.ArrayList<>());
        returnOrder.getActivities().add(act);
    }
}