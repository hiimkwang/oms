package com.oms.module.returnorder.service;

import com.oms.config.exception.BusinessException;
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
        Order originalOrder = orderRepo.findByOrderCode(request.getOriginalOrderCode()).orElseThrow(() -> new BusinessException("Không tìm thấy đơn gốc!"));

        // GUARD 1: Chỉ cho phép trả hàng với đơn đã HOÀN THÀNH (đã giao/đã bán).
        if (!com.oms.constant.CommonConstants.OrderStatusConstant.COMPLETED.equals(originalOrder.getStatus())) {
            throw new BusinessException("Chỉ có thể tạo phiếu trả hàng cho đơn hàng đã hoàn thành!");
        }

        // GUARD 2: Phải có chi tiết hàng trả hợp lệ.
        if (request.getDetails() == null || request.getDetails().isEmpty()) {
            throw new BusinessException("Phiếu trả hàng phải có ít nhất 1 sản phẩm!");
        }

        String user = getCurrentUserName();
        String returnCode = "TH" + System.currentTimeMillis() + "-" + java.util.UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        // ---------------------------------------------------------------------
        // LẤY GIÁ & SỐ LƯỢNG TỪ ĐƠN GỐC (SERVER-SIDE) — KHÔNG tin đơn giá client gửi.
        // Số tiền hoàn = đơn giá gốc TRỪ chiết khấu phân bổ theo tỷ lệ tiền hàng
        // (phản ánh đúng số khách THỰC trả, tránh hoàn nhiều hơn số đã thu).
        // ---------------------------------------------------------------------
        java.util.Map<String, Integer> orderedQtyBySku = new java.util.HashMap<>();
        java.util.Map<String, BigDecimal> unitPriceBySku = new java.util.HashMap<>();
        java.util.Map<String, String> nameBySku = new java.util.HashMap<>();
        BigDecimal itemsGross = BigDecimal.ZERO; // tổng tiền hàng trước chiết khấu
        if (originalOrder.getDetails() != null) {
            for (com.oms.module.order.entity.OrderDetail od : originalOrder.getDetails()) {
                if (od.getSku() == null) continue;
                int q = od.getQuantity() != null ? od.getQuantity() : 0;
                BigDecimal up = od.getUnitPrice() != null ? od.getUnitPrice() : BigDecimal.ZERO;
                orderedQtyBySku.merge(od.getSku(), q, Integer::sum);
                unitPriceBySku.putIfAbsent(od.getSku(), up);
                nameBySku.putIfAbsent(od.getSku(), od.getProductName());
                itemsGross = itemsGross.add(up.multiply(BigDecimal.valueOf(q)));
            }
        }

        // Số lượng đã trả trước đó (mọi phiếu CHƯA bị từ chối) theo SKU -> chặn trả vượt tổng đã mua khi cộng dồn.
        java.util.Map<String, Integer> alreadyReturnedBySku = new java.util.HashMap<>();
        for (ReturnOrder prev : returnRepo.findByOriginalOrder_OrderCode(originalOrder.getOrderCode())) {
            if (REJECTED.equals(prev.getStatus()) || prev.getDetails() == null) continue;
            for (ReturnOrderDetail pd : prev.getDetails()) {
                if (pd.getSku() == null) continue;
                alreadyReturnedBySku.merge(pd.getSku(), pd.getQuantity() != null ? pd.getQuantity() : 0, Integer::sum);
            }
        }

        BigDecimal orderDiscount = originalOrder.getDiscountAmount() != null ? originalOrder.getDiscountAmount() : BigDecimal.ZERO;

        ReturnOrder returnOrder = ReturnOrder.builder().returnCode(returnCode).originalOrder(originalOrder).reason(request.getReason()).note(request.getNote()).returnFee(request.getReturnFee() != null ? request.getReturnFee() : BigDecimal.ZERO) // Phí khách chịu
                .shopReturnFee(request.getShopReturnFee() != null ? request.getShopReturnFee() : BigDecimal.ZERO).refundStatus(UNPAID).restockStatus(RESTOCK_PENDING).status(PENDING).createdBy(user).build();

        BigDecimal totalRefund = BigDecimal.ZERO;
        List<ReturnOrderDetail> details = new ArrayList<>();
        java.util.Map<String, Integer> thisReturnBySku = new java.util.HashMap<>();

        for (ReturnOrderRequest.ReturnItemRequest item : request.getDetails()) {
            String sku = item.getSku();
            int qty = item.getQuantity() != null ? item.getQuantity() : 0;
            if (qty <= 0) {
                throw new BusinessException("Số lượng trả của sản phẩm [" + item.getProductName() + "] phải lớn hơn 0!");
            }
            if (sku == null || !orderedQtyBySku.containsKey(sku)) {
                throw new BusinessException("Sản phẩm [" + item.getProductName() + "] không có trong đơn gốc, không thể trả!");
            }
            int ordered = orderedQtyBySku.getOrDefault(sku, 0);
            int already = alreadyReturnedBySku.getOrDefault(sku, 0);
            if (already + qty > ordered) {
                throw new BusinessException("Số lượng trả của [" + nameBySku.get(sku) + "] vượt quá số còn được trả (đã mua "
                        + ordered + ", đã trả " + already + ")!");
            }
            thisReturnBySku.merge(sku, qty, Integer::sum);

            // Đơn giá gốc theo SKU (lấy từ đơn, KHÔNG lấy từ client)
            BigDecimal unitPrice = unitPriceBySku.getOrDefault(sku, BigDecimal.ZERO);
            // Giá hoàn thực / đơn vị = đơn giá gốc - chiết khấu phân bổ theo tỷ lệ (orderDiscount * unitPrice / itemsGross)
            BigDecimal netUnit = unitPrice;
            if (orderDiscount.signum() > 0 && itemsGross.signum() > 0) {
                BigDecimal allocPerUnit = orderDiscount.multiply(unitPrice)
                        .divide(itemsGross, 2, java.math.RoundingMode.HALF_UP);
                netUnit = unitPrice.subtract(allocPerUnit);
                if (netUnit.signum() < 0) netUnit = BigDecimal.ZERO;
            }
            BigDecimal lineRefund = netUnit.multiply(BigDecimal.valueOf(qty));
            totalRefund = totalRefund.add(lineRefund);

            details.add(ReturnOrderDetail.builder().returnOrder(returnOrder).sku(sku)
                    .productName(nameBySku.getOrDefault(sku, item.getProductName()))
                    .quantity(qty).unitPrice(unitPrice).refundAmount(lineRefund).build());
        }

        // Không cho số tiền hoàn về âm khi phí trả hàng lớn hơn giá trị hàng hoàn.
        BigDecimal refundAfterFee = totalRefund.subtract(returnOrder.getReturnFee());
        returnOrder.setTotalRefundAmount(refundAfterFee.compareTo(BigDecimal.ZERO) > 0 ? refundAfterFee : BigDecimal.ZERO);
        returnOrder.setDetails(details);

        // --- TRẢ TOÀN PHẦN vs TRẢ MỘT PHẦN ---
        // Chỉ chuyển đơn gốc sang RETURNED khi TẤT CẢ sản phẩm đã được trả hết (cộng dồn mọi phiếu),
        // vì báo cáo doanh thu/COGS LOẠI trạng thái RETURNED. Trả một phần -> giữ nguyên trạng thái đơn gốc
        // để phần hàng khách GIỮ LẠI vẫn được ghi nhận doanh thu/giá vốn.
        boolean fullyReturned = true;
        for (var e : orderedQtyBySku.entrySet()) {
            int already = alreadyReturnedBySku.getOrDefault(e.getKey(), 0);
            int now = thisReturnBySku.getOrDefault(e.getKey(), 0);
            if (already + now < e.getValue()) {
                fullyReturned = false;
                break;
            }
        }

        if (originalOrder.getActivities() == null) {
            originalOrder.setActivities(new java.util.ArrayList<>());
        }
        if (fullyReturned) {
            originalOrder.setStatus(RETURNED);
            originalOrder.getActivities().add(OrderActivity.builder().order(originalOrder).action("Chuyển trạng thái")
                    .description("Đơn hàng chuyển sang TRẢ HÀNG (trả toàn bộ) do phiếu: " + returnCode)
                    .createdBy(user).createdAt(java.time.LocalDateTime.now()).build());
            orderRepo.save(originalOrder);
            try {
                notificationService.create("Đơn hàng bị trả lại: " + originalOrder.getOrderCode(), "Đơn hàng đã được chuyển sang trạng thái TRẢ HÀNG.", Notification.NotificationType.ORDER, "/ui/orders/detail/" + originalOrder.getOrderCode());
            } catch (Exception e) {
                log.error("Lỗi Noti đơn hàng: {}", e.getMessage());
            }
        } else {
            originalOrder.getActivities().add(OrderActivity.builder().order(originalOrder).action("Trả hàng một phần")
                    .description("Phiếu trả hàng một phần: " + returnCode + " (đơn gốc giữ trạng thái để ghi nhận phần khách giữ lại)")
                    .createdBy(user).createdAt(java.time.LocalDateTime.now()).build());
            orderRepo.save(originalOrder);
        }

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
            log.error("Lỗi khi gửi thông báo trả hàng: {}", e.getMessage());
        }

        return savedReturnOrder;
    }

    public ReturnOrder getByCode(String code) {
        return returnRepo.findByReturnCode(code).orElseThrow(() -> new BusinessException("Không tìm thấy phiếu trả hàng: " + code));
    }

    public List<ReturnOrder> getAllReturns(String keyword, String status) {
        // Lọc + sắp xếp + JOIN FETCH đơn gốc ở tầng DB (tránh tải toàn bộ bảng + N+1 truy vấn đơn gốc).
        return returnRepo.search(status, keyword);
    }

    @Transactional
    public void processRefund(Long returnOrderId, String method) {
        ReturnOrder returnOrder = returnRepo.findByIdForUpdate(returnOrderId).orElseThrow(() -> new BusinessException("Không tìm thấy phiếu trả hàng"));

        if (REJECTED.equals(returnOrder.getStatus())) {
            throw new BusinessException("Phiếu trả hàng đã bị từ chối, không thể hoàn tiền!");
        }
        if (REFUNDED.equals(returnOrder.getRefundStatus())) {
            throw new BusinessException("Phiếu này đã được hoàn tiền!");
        }

        // Validate hình thức thanh toán hợp lệ trước khi tạo phiếu chi
        CashTransaction.PaymentMethod paymentMethod;
        try {
            paymentMethod = CashTransaction.PaymentMethod.valueOf(method);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BusinessException("Hình thức thanh toán không hợp lệ!");
        }

        // 1. TẠO PHIẾU CHI: HOÀN TIỀN CHO KHÁCH (Như cũ)
        if (returnOrder.getTotalRefundAmount().compareTo(BigDecimal.ZERO) > 0) {
            com.oms.module.cashbook.entity.CashTransaction refundPayment = com.oms.module.cashbook.entity.CashTransaction.builder().code("PC-HT-" + System.currentTimeMillis() + "-" + java.util.UUID.randomUUID().toString().substring(0, 4).toUpperCase()).type(com.oms.module.cashbook.entity.CashTransaction.TransactionType.PAYMENT).paymentMethod(paymentMethod).targetGroup(com.oms.module.cashbook.entity.CashTransaction.TargetGroup.CUSTOMER).targetId(returnOrder.getOriginalOrder().getCustomer() != null ? returnOrder.getOriginalOrder().getCustomer().getId() : null).targetName(returnOrder.getOriginalOrder().getCustomer() != null ? returnOrder.getOriginalOrder().getCustomer().getFullName() : "Khách trả hàng").amount(returnOrder.getTotalRefundAmount()).reason("Hoàn tiền cho khách").description("Hoàn tiền phiếu trả hàng " + returnOrder.getReturnCode() + " (Đơn gốc: " + returnOrder.getOriginalOrder().getOrderCode() + ")").transactionDate(java.time.LocalDateTime.now()).creatorName(getCurrentUserName()).build();
            cashRepo.save(refundPayment);
        }

        // 2. TẠO PHIẾU CHI (MỚI): CHI PHÍ PHÁT SINH SHOP TỰ CHỊU
        if (returnOrder.getShopReturnFee() != null && returnOrder.getShopReturnFee().compareTo(BigDecimal.ZERO) > 0) {
            com.oms.module.cashbook.entity.CashTransaction shopFeePayment = com.oms.module.cashbook.entity.CashTransaction.builder().code("PC-PH-" + System.currentTimeMillis() + "-" + java.util.UUID.randomUUID().toString().substring(0, 4).toUpperCase())
                    .type(com.oms.module.cashbook.entity.CashTransaction.TransactionType.PAYMENT).paymentMethod(paymentMethod).targetGroup(com.oms.module.cashbook.entity.CashTransaction.TargetGroup.OTHER) // Ghi nhận là chi phí Khác/Nội bộ
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
        ReturnOrder returnOrder = returnRepo.findByIdForUpdate(returnOrderId).orElseThrow(() -> new BusinessException("Không tìm thấy phiếu trả hàng"));

        if (REJECTED.equals(returnOrder.getStatus())) {
            throw new BusinessException("Phiếu trả hàng đã bị từ chối, không thể nhập kho!");
        }
        if (RESTOCK_RESTOCKED.equals(returnOrder.getRestockStatus())) {
            throw new BusinessException("Phiếu này đã được nhập kho!");
        }
        if (branchId == null) {
            throw new BusinessException("Vui lòng chọn chi nhánh nhập hàng trả về!");
        }

        // Khóa hàng theo thứ tự SKU cố định để tránh deadlock
        List<com.oms.module.returnorder.entity.ReturnOrderDetail> lockOrder = new ArrayList<>(returnOrder.getDetails());
        lockOrder.sort(java.util.Comparator.comparing(d -> d.getSku() == null ? "" : d.getSku()));
        List<String> skippedSkus = new ArrayList<>();
        for (com.oms.module.returnorder.entity.ReturnOrderDetail detail : lockOrder) {
            com.oms.module.product.entity.ProductVariant variant = variantRepo.findBySkuForUpdate(detail.getSku()).orElse(null);
            if (variant != null) {
                variant.setStockQuantity((variant.getStockQuantity() != null ? variant.getStockQuantity() : 0) + detail.getQuantity());
                variantRepo.save(variant);

                com.oms.module.inventory.entity.Inventory inv = inventoryRepo.findByVariantIdAndBranchIdForUpdate(variant.getId(), branchId).orElse(com.oms.module.inventory.entity.Inventory.builder().variantId(variant.getId()).branchId(branchId).stock(0).availableStock(0).inboundStock(0).build());
                inv.setStock((inv.getStock() != null ? inv.getStock() : 0) + detail.getQuantity());
                inv.setAvailableStock((inv.getAvailableStock() != null ? inv.getAvailableStock() : 0) + detail.getQuantity());
                inventoryRepo.save(inv);
            } else {
                // Không tìm thấy biến thể (SKU đã bị xóa/đổi): KHÔNG im lặng bỏ qua -> ghi lại để cảnh báo.
                log.warn("Restock: không tìm thấy biến thể SKU {} của phiếu {}, hàng KHÔNG được nhập lại kho.",
                        detail.getSku(), returnOrder.getReturnCode());
                skippedSkus.add(detail.getSku());
            }
        }

        returnOrder.setRestockStatus(RESTOCK_RESTOCKED);
        checkAndCompleteReturn(returnOrder);

        String restockDesc = "Đã cất lại hàng vào kho chi nhánh ID: " + branchId;
        if (!skippedSkus.isEmpty()) {
            restockDesc += " | CẢNH BÁO: không nhập lại được các SKU không còn tồn tại: " + String.join(", ", skippedSkus);
        }
        logActivity(returnOrder, "Nhận hàng vào kho", restockDesc);
        returnRepo.save(returnOrder);
    }

    private void checkAndCompleteReturn(ReturnOrder returnOrder) {
        if (REFUNDED.equals(returnOrder.getRefundStatus()) && RESTOCK_RESTOCKED.equals(returnOrder.getRestockStatus())) {
            returnOrder.setStatus(COMPLETED);
            logActivity(returnOrder, "Hoàn tất phiếu trả hàng", "Đã xử lý xong các bước hoàn tiền và nhập kho.");

            try {
                notificationService.create("Hoàn tất trả hàng: " + returnOrder.getReturnCode(), "Đã xử lý xong hoàn tiền và nhập lại kho cho đơn gốc " + returnOrder.getOriginalOrder().getOrderCode(), Notification.NotificationType.RETURN, "/ui/returns/" + returnOrder.getReturnCode());
            } catch (Exception e) {
                log.error("Lỗi khi gửi thông báo hoàn tất trả hàng: {}", e.getMessage());
            }

        }
    }

    @Transactional
    public void deleteBulk(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        for (Long id : ids) {
            ReturnOrder ro = returnRepo.findById(id).orElse(null);
            if (ro == null) continue;
            // Không cho xóa phiếu đã hoàn tiền hoặc đã nhập kho (đã tác động quỹ/tồn) -> tránh mất dấu vết, lệch số
            if (REFUNDED.equals(ro.getRefundStatus()) || RESTOCK_RESTOCKED.equals(ro.getRestockStatus())) {
                throw new BusinessException("Phiếu trả hàng " + ro.getReturnCode() + " đã hoàn tiền/nhập kho nên không thể xóa.");
            }
            returnRepo.delete(ro);
        }
    }

    private void logActivity(ReturnOrder returnOrder, String action, String description) {
        com.oms.module.returnorder.entity.ReturnActivity act = com.oms.module.returnorder.entity.ReturnActivity.builder().returnOrder(returnOrder).action(action).description(description).createdBy(getCurrentUserName()).build();
        if (returnOrder.getActivities() == null) returnOrder.setActivities(new java.util.ArrayList<>());
        returnOrder.getActivities().add(act);
    }
}