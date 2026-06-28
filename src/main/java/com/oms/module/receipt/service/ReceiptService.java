package com.oms.module.receipt.service;

import com.oms.module.account.entity.User;
import com.oms.module.cashbook.dto.CashTransactionRequest;
import com.oms.module.cashbook.entity.CashTransaction;
import com.oms.module.cashbook.service.CashbookService;
import com.oms.module.inventory.entity.Inventory;
import com.oms.module.inventory.repository.InventoryRepository;
import com.oms.module.notification.entity.Notification;
import com.oms.module.notification.repository.NotificationRepository;
import com.oms.module.product.entity.ProductVariant;
import com.oms.module.product.repository.ProductVariantRepository;
import com.oms.module.product.service.ProductService;
import com.oms.module.receipt.dto.ReceiptRequest;
import com.oms.module.receipt.dto.SupplierStatsResponse;
import com.oms.module.receipt.entity.Receipt;
import com.oms.module.receipt.entity.ReceiptActivity;
import com.oms.module.receipt.entity.ReceiptDetail;
import com.oms.module.receipt.repository.ReceiptActivityRepository;
import com.oms.module.receipt.repository.ReceiptRepository;
import com.oms.module.setting.entity.Branch;
import com.oms.module.setting.repository.BranchRepository;
import com.oms.module.supplier.entity.Supplier;
import com.oms.module.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.oms.constant.CommonConstants.PaymentStatusConstant.*;
import static com.oms.constant.CommonConstants.ReceiptStatusConstant.PENDING;
import static com.oms.constant.CommonConstants.ReceiptStatusConstant.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReceiptService {
    private final ReceiptRepository receiptRepository;
    private final SupplierRepository supplierRepository;
    private final ProductVariantRepository variantRepository;
    private final ReceiptActivityRepository activityRepository;
    private final InventoryRepository inventoryRepository;
    private final BranchRepository branchRepository;
    private final ProductService productService;
    private final CashbookService cashbookService;
    private final NotificationRepository notificationRepository;

    private BigDecimal round(BigDecimal value) {
        return value != null ? value.setScale(0, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    private String getCurrentUserName() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof User) {
                return ((User) principal).getFullName();
            }
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "Hệ thống";
        }
    }

    private String getFullProductName(String sku) {
        return variantRepository.findBySku(sku).map(variant -> {
            String name = variant.getProduct() != null ? variant.getProduct().getName() : sku;
            if (variant.getVariantName() != null && !"Mặc định".equalsIgnoreCase(variant.getVariantName())) {
                name += " - " + variant.getVariantName();
            }
            return name;
        }).orElse(sku);
    }

    private void pushNotification(String title, String content, String link) {
        try {
            Notification noti = new Notification();
            noti.setTitle(title);
            noti.setType(Notification.NotificationType.IMPORT);
            noti.setMessage(content);
            noti.setLink(link);
            noti.setRead(false);
            noti.setCreatedAt(LocalDateTime.now());
            notificationRepository.save(noti);
        } catch (Exception e) {
            log.error("Lỗi khi lưu thông báo: {}", e.getMessage());
        }
    }

    private void createPaymentVoucher(Receipt receipt, BigDecimal amount, String methodStr) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) return;

        CashTransactionRequest cashReq = new CashTransactionRequest();
        cashReq.setType(CashTransaction.TransactionType.PAYMENT);

        CashTransaction.PaymentMethod pm = CashTransaction.PaymentMethod.CASH;
        if ("TRANSFER".equalsIgnoreCase(methodStr) || "BANK".equalsIgnoreCase(methodStr)) {
            pm = CashTransaction.PaymentMethod.BANK;
        }
        cashReq.setPaymentMethod(pm);

        cashReq.setTargetGroup(CashTransaction.TargetGroup.SUPPLIER);
        if (receipt.getSupplier() != null) {
            cashReq.setTargetId(receipt.getSupplier().getId());
        }

        cashReq.setAmount(amount);
        cashReq.setReason("Trả nợ nhà cung cấp");
        cashReq.setDescription("Chi tiền trả nhà cung cấp cho phiếu nhập " + receipt.getCode());
        cashReq.setBranchId(receipt.getBranchId());
        cashReq.setReferenceCode(receipt.getCode());
        cashReq.setTransactionDate(LocalDateTime.now());

        cashbookService.createTransaction(cashReq);
    }

    @Transactional
    public Receipt createReceipt(ReceiptRequest request) {
        Supplier supplier = supplierRepository.findByCode(request.getSupplierCode()).orElseThrow(() -> new RuntimeException("Không tìm thấy NCC"));

        String currentWorker = getCurrentUserName();
        String actualBranchName = "Kho mặc định";
        if (request.getBranchId() != null) {
            actualBranchName = branchRepository.findById(request.getBranchId()).map(Branch::getName).orElse("Kho mặc định");
        }

        Receipt receipt = Receipt.builder().code("REI" + System.currentTimeMillis() + java.util.UUID.randomUUID().toString().substring(0, 4).toUpperCase()).supplier(supplier).branchId(request.getBranchId()).branchName(actualBranchName).note(request.getNote()).itemsAmount(round(request.getItemsAmount())).discount(round(request.getDiscount())).shippingFee(round(request.getShippingFee())).totalAmount(round(request.getTotalAmount())).amountPaid(round(request.getAmountPaid())).paymentStatus(request.getPaymentStatus()).createdAt(request.getCreatedAt() != null ? request.getCreatedAt() : LocalDateTime.now()).status(TRADING).importStatus(PENDING).creatorName(currentWorker).build();

        List<ReceiptDetail> details = request.getItems().stream().map(item -> ReceiptDetail.builder().receipt(receipt).sku(item.getSku()).productName(getFullProductName(item.getSku())).quantity(item.getQuantity()).importPrice(round(item.getImportPrice())).warrantyMonths(item.getWarrantyMonths() != null ? item.getWarrantyMonths() : 0).build()).collect(Collectors.toList());

        receipt.setDetails(details);
        Receipt savedReceipt = receiptRepository.save(receipt);

        logActivity(savedReceipt, "Tạo mới phiếu nhập hàng", currentWorker);

        pushNotification("Đơn nhập hàng mới", "Phiếu nhập " + savedReceipt.getCode() + " vừa được tạo trên hệ thống.", "/ui/imports/" + savedReceipt.getCode());

        if (savedReceipt.getAmountPaid().compareTo(BigDecimal.ZERO) > 0) {
            createPaymentVoucher(savedReceipt, savedReceipt.getAmountPaid(), String.valueOf(request.getPaymentMethod()));
        }

        // Luôn ghi nhận "hàng đang về" (inbound) khi tạo phiếu để kế toán inbound đối xứng.
        addInboundStock(savedReceipt, details);

        // Nếu yêu cầu nhập kho ngay: xác nhận nhập (sẽ trừ đúng phần inbound vừa cộng và cộng tồn thực tế).
        if (Boolean.TRUE.equals(request.getIsImportStock())) {
            this.confirmImport(savedReceipt.getId());
        }

        return savedReceipt;
    }

    @Transactional
    public Receipt updateReceipt(String code, ReceiptRequest request) {
        Receipt receipt = receiptRepository.findByCode(code).orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu mã: " + code));

        if (COMPLETED.equals(receipt.getStatus()) || COMPLETED.equals(receipt.getImportStatus())) {
            throw new RuntimeException("Đã nhập kho hoặc hoàn thành không được sửa!");
        }

        Supplier supplier = supplierRepository.findByCode(request.getSupplierCode()).orElseThrow(() -> new RuntimeException("Không tìm thấy NCC"));

        removeInboundStock(receipt, receipt.getDetails());

        String actualBranchName = "Kho mặc định";
        if (request.getBranchId() != null) {
            actualBranchName = branchRepository.findById(request.getBranchId()).map(Branch::getName).orElse("Kho mặc định");
        }

        receipt.setSupplier(supplier);
        receipt.setBranchId(request.getBranchId());
        receipt.setBranchName(actualBranchName);
        receipt.setNote(request.getNote());

        receipt.setItemsAmount(round(request.getItemsAmount()));
        receipt.setDiscount(round(request.getDiscount()));
        receipt.setShippingFee(round(request.getShippingFee()));
        receipt.setTotalAmount(round(request.getTotalAmount()));

        BigDecimal paid = receipt.getAmountPaid() != null ? receipt.getAmountPaid() : BigDecimal.ZERO;
        if (paid.compareTo(receipt.getTotalAmount()) >= 0) receipt.setPaymentStatus(PAID);
        else if (paid.compareTo(BigDecimal.ZERO) > 0) receipt.setPaymentStatus(PARTIAL);
        else receipt.setPaymentStatus(UNPAID);

        receipt.getDetails().clear();
        List<ReceiptDetail> newDetails = request.getItems().stream().map(item -> ReceiptDetail.builder().receipt(receipt).sku(item.getSku()).productName(getFullProductName(item.getSku())).quantity(item.getQuantity()).importPrice(round(item.getImportPrice())).warrantyMonths(item.getWarrantyMonths() != null ? item.getWarrantyMonths() : 0).build()).collect(Collectors.toList());

        receipt.getDetails().addAll(newDetails);
        Receipt savedReceipt = receiptRepository.save(receipt);
        logActivity(receipt, "Cập nhật thông tin phiếu nhập", getCurrentUserName());

        pushNotification("Cập nhật đơn nhập hàng", "Phiếu nhập " + receipt.getCode() + " đã được chỉnh sửa thông tin.", "/ui/imports/" + receipt.getCode());

        addInboundStock(savedReceipt, newDetails);

        return savedReceipt;
    }

    @Transactional
    public Receipt confirmImport(Long id) {
        // Khóa ghi dòng phiếu nhập để 2 request đồng thời không cùng cộng kho (idempotent + chống nhập đôi)
        Receipt receipt = receiptRepository.findByIdForUpdate(id).orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập"));
        if (COMPLETED.equals(receipt.getImportStatus())) {
            throw new RuntimeException("Đơn này đã nhập kho rồi!");
        }

        String currentWorker = getCurrentUserName();
        // Phân bổ phí ship & chiết khấu của phiếu vào GIÁ VỐN (landed cost) theo tỉ lệ giá nhập.
        // ratio = (phí ship − chiết khấu) / tiền hàng. Có thể âm khi chiết khấu > phí (làm giảm giá vốn).
        BigDecimal ratio = calculateExtraCostRatio(receipt);

        // Khóa hàng theo thứ tự SKU cố định để tránh deadlock khi nhiều phiếu chạy đồng thời
        List<ReceiptDetail> lockOrder = new java.util.ArrayList<>(receipt.getDetails());
        lockOrder.sort(java.util.Comparator.comparing(d -> d.getSku() == null ? "" : d.getSku()));
        for (ReceiptDetail detail : lockOrder) {
            ProductVariant variant = variantRepository.findBySkuForUpdate(detail.getSku()).orElseThrow(() -> new RuntimeException("Không thấy SP mã: " + detail.getSku()));

            // Giá vốn thực tế mỗi sản phẩm = giá nhập + phần phí/chiết khấu phân bổ
            BigDecimal extraCostPerItem = detail.getImportPrice().multiply(ratio);
            BigDecimal actualImportPrice = detail.getImportPrice().add(extraCostPerItem);

            updateMAC(variant, detail.getQuantity(), actualImportPrice);
            updateBranchInventoryForImport(receipt.getBranchId(), variant.getId(), detail.getQuantity());

            int currentVariantStock = variant.getStockQuantity() != null ? variant.getStockQuantity() : 0;
            variant.setStockQuantity(currentVariantStock + detail.getQuantity());
            variantRepository.save(variant);

            if (variant.getProduct() != null) {
                productService.syncProductTotalStock(variant.getProduct().getId());
            }
        }

        receipt.setImportStatus(COMPLETED);
        logActivity(receipt, "Xác nhận nhập kho vào: " + receipt.getBranchName(), currentWorker);

        pushNotification("Nhập kho thành công", "Hàng hóa của phiếu nhập " + receipt.getCode() + " đã được cộng vào " + receipt.getBranchName(), "/ui/imports/" + receipt.getCode());

        checkAndCompleteReceipt(receipt);
        return receiptRepository.save(receipt);
    }

    @Transactional
    public void cancelReceipt(Long id) {
        Receipt receipt = receiptRepository.findById(id).orElseThrow();
        if (COMPLETED.equals(receipt.getImportStatus())) throw new RuntimeException("Không thể hủy đơn đã nhập kho");

        receipt.setStatus(CANCELLED);
        removeInboundStock(receipt, receipt.getDetails());

        // Đảo các phiếu chi đã trả NCC cho phiếu nhập này để quỹ không bị "chi khống"
        cashbookService.reversePaymentsByReference(receipt.getCode());

        receiptRepository.save(receipt);
        logActivity(receipt, "Hủy phiếu nhập hàng", getCurrentUserName());

        pushNotification("Hủy phiếu nhập hàng", "Phiếu nhập " + receipt.getCode() + " đã bị hủy bỏ.", "/ui/imports/" + receipt.getCode());
    }

    private void addInboundStock(Receipt receipt, List<ReceiptDetail> details) {
        if (receipt.getBranchId() == null) return;
        for (ReceiptDetail detail : details) {
            ProductVariant variant = variantRepository.findBySku(detail.getSku()).orElse(null);
            if (variant == null) continue;

            Inventory inv = inventoryRepository.findByVariantIdAndBranchIdForUpdate(variant.getId(), receipt.getBranchId()).orElse(Inventory.builder().variantId(variant.getId()).branchId(receipt.getBranchId()).stock(0).availableStock(0).inboundStock(0).build());

            int currentInbound = inv.getInboundStock() != null ? inv.getInboundStock() : 0;
            inv.setInboundStock(currentInbound + detail.getQuantity());
            inventoryRepository.save(inv);
        }
    }

    private void removeInboundStock(Receipt receipt, List<ReceiptDetail> details) {
        if (receipt.getBranchId() == null) return;
        for (ReceiptDetail detail : details) {
            ProductVariant variant = variantRepository.findBySku(detail.getSku()).orElse(null);
            if (variant == null) continue;

            inventoryRepository.findByVariantIdAndBranchIdForUpdate(variant.getId(), receipt.getBranchId()).ifPresent(inv -> {
                int currentInbound = inv.getInboundStock() != null ? inv.getInboundStock() : 0;
                inv.setInboundStock(Math.max(0, currentInbound - detail.getQuantity()));
                inventoryRepository.save(inv);
            });
        }
    }

    private void updateBranchInventoryForImport(Long branchId, Long variantId, Integer qty) {
        if (branchId == null) return;

        Inventory inv = inventoryRepository.findByVariantIdAndBranchIdForUpdate(variantId, branchId).orElse(Inventory.builder().variantId(variantId).branchId(branchId).stock(0).availableStock(0).inboundStock(0).build());

        int currentInbound = inv.getInboundStock() != null ? inv.getInboundStock() : 0;
        inv.setInboundStock(Math.max(0, currentInbound - qty));
        inv.setStock((inv.getStock() != null ? inv.getStock() : 0) + qty);
        inv.setAvailableStock((inv.getAvailableStock() != null ? inv.getAvailableStock() : 0) + qty);

        inventoryRepository.save(inv);
    }

    private void updateMAC(ProductVariant variant, int importQty, BigDecimal actualPrice) {
        // Dùng tồn cũ ĐÃ KẸP về >= 0 một cách NHẤT QUÁN ở cả tử số và mẫu số (tránh sai lệch giá vốn)
        int oldStock = Math.max(0, variant.getStockQuantity() != null ? variant.getStockQuantity() : 0);
        BigDecimal oldCost = variant.getCostPrice() != null ? variant.getCostPrice() : BigDecimal.ZERO;
        BigDecimal price = actualPrice != null ? actualPrice : BigDecimal.ZERO;

        int totalQty = oldStock + importQty;
        if (totalQty > 0) {
            BigDecimal totalOldValue = oldCost.multiply(new BigDecimal(oldStock));
            BigDecimal totalNewValue = price.multiply(new BigDecimal(importQty));
            // Giữ 2 chữ số thập phân (đúng theo cột cost_price DECIMAL(15,2)), tránh mất chính xác khi nhập nhiều lần
            BigDecimal newCost = totalOldValue.add(totalNewValue).divide(new BigDecimal(totalQty), 2, RoundingMode.HALF_UP);
            variant.setCostPrice(newCost);
        } else {
            variant.setCostPrice(price.setScale(2, RoundingMode.HALF_UP));
        }
    }

    private BigDecimal calculateExtraCostRatio(Receipt receipt) {
        if (receipt.getItemsAmount() == null || receipt.getItemsAmount().compareTo(BigDecimal.ZERO) <= 0)
            return BigDecimal.ZERO;
        BigDecimal fee = receipt.getShippingFee() != null ? receipt.getShippingFee() : BigDecimal.ZERO;
        BigDecimal discount = receipt.getDiscount() != null ? receipt.getDiscount() : BigDecimal.ZERO;
        return fee.subtract(discount).divide(receipt.getItemsAmount(), 4, RoundingMode.HALF_UP);
    }

    @Transactional
    public void addPayment(Long id, BigDecimal amountToAdd, String method) {
        Receipt receipt = receiptRepository.findById(id).orElseThrow();
        String currentWorker = getCurrentUserName();

        BigDecimal amountToAddRounded = round(amountToAdd);
        if (amountToAddRounded.compareTo(BigDecimal.ZERO) <= 0) throw new RuntimeException("Số tiền phải > 0");

        BigDecimal currentPaid = receipt.getAmountPaid() != null ? receipt.getAmountPaid() : BigDecimal.ZERO;
        BigDecimal totalAmount = receipt.getTotalAmount() != null ? receipt.getTotalAmount() : BigDecimal.ZERO;

        BigDecimal newPaidAmount = currentPaid.add(amountToAddRounded);
        receipt.setAmountPaid(newPaidAmount);

        if (newPaidAmount.compareTo(totalAmount) >= 0) {
            receipt.setPaymentStatus(PAID);
            if (COMPLETED.equals(receipt.getImportStatus())) receipt.setStatus(COMPLETED);
        } else {
            receipt.setPaymentStatus(PARTIAL);
        }

        receiptRepository.save(receipt);
        logActivity(receipt, "Thanh toán: " + amountToAddRounded + "đ (" + method + ")", currentWorker);

        pushNotification("Thanh toán phiếu nhập", "Ghi nhận thanh toán " + amountToAddRounded + "đ cho phiếu nhập " + receipt.getCode(), "/ui/imports/" + receipt.getCode());

        createPaymentVoucher(receipt, amountToAddRounded, method);
    }

    private void checkAndCompleteReceipt(Receipt receipt) {
        if (COMPLETED.equals(receipt.getImportStatus()) && PAID.equals(receipt.getPaymentStatus())) {
            receipt.setStatus(COMPLETED);
        }
    }

    @Transactional(readOnly = true)
    public SupplierStatsResponse getSupplierStats(String code, LocalDateTime start, LocalDateTime end) {
        List<Receipt> receipts;
        BigDecimal totalDebt;
        long totalOrders;
        BigDecimal totalAmount;

        if (start == null || end == null) {
            receipts = receiptRepository.findBySupplierCodeOrderByCreatedAtDesc(code);
            totalDebt = receiptRepository.getTotalDebtAllTime(code);
        } else {
            receipts = receiptRepository.findBySupplierCodeAndCreatedAtBetweenOrderByCreatedAtDesc(code, start, end);
            totalDebt = receiptRepository.getTotalDebt(code, start, end);
        }

        totalOrders = receipts.size();
        totalAmount = receipts.stream().map(r -> r.getTotalAmount() != null ? r.getTotalAmount() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);

        List<SupplierStatsResponse.ReceiptSummary> history = receipts.stream().map(r -> SupplierStatsResponse.ReceiptSummary.builder().code(r.getCode()).createdAt(r.getCreatedAt()).status(r.getStatus()).paymentStatus(r.getPaymentStatus()).totalAmount(r.getTotalAmount()).build()).collect(Collectors.toList());

        return SupplierStatsResponse.builder().totalOrders(totalOrders).totalAmount(round(totalAmount)).totalDebt(round(totalDebt)).history(history).build();
    }

    private void logActivity(Receipt receipt, String action, String creator) {
        ReceiptActivity activity = new ReceiptActivity();
        activity.setReceipt(receipt);
        activity.setCreatorName(creator);
        activity.setAction(action);
        activityRepository.save(activity);
    }

    public List<Receipt> getAllReceipts() {
        return receiptRepository.findAll();
    }

    /**
     * Xóa hàng loạt phiếu nhập. Không cho xóa phiếu ĐÃ NHẬP KHO (importStatus=COMPLETED)
     * để tránh phải đảo ngược tồn kho thực tế. Với phiếu chưa nhập kho thì hoàn lại phần
     * "hàng đang về" (inbound) đã cộng khi tạo phiếu.
     */
    @Transactional
    public void deleteBulk(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        for (Long id : ids) {
            Receipt receipt = receiptRepository.findById(id).orElse(null);
            if (receipt == null) continue;
            if (COMPLETED.equals(receipt.getImportStatus())) {
                throw new RuntimeException("Phiếu nhập " + receipt.getCode() + " đã nhập kho nên không thể xóa. Hãy dùng chức năng khác để điều chỉnh tồn kho.");
            }
            // Hoàn lại inbound nếu phiếu chưa bị hủy (phiếu CANCELLED đã trừ inbound khi hủy)
            if (!CANCELLED.equals(receipt.getStatus())) {
                removeInboundStock(receipt, receipt.getDetails());
            }
            receiptRepository.delete(receipt);
        }
    }

    @Transactional(readOnly = true)
    public Receipt getReceiptById(Long id) {
        Receipt receipt = receiptRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập"));
        populateImageUrls(receipt);
        return receipt;
    }

    @Transactional(readOnly = true)
    public Receipt getReceiptByCode(String code) {
        Receipt receipt = receiptRepository.findByCode(code).orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập: " + code));
        populateImageUrls(receipt);
        return receipt;
    }

    private void populateImageUrls(Receipt receipt) {
        if (receipt.getDetails() != null) {
            for (ReceiptDetail detail : receipt.getDetails()) {
                variantRepository.findBySku(detail.getSku()).ifPresent(variant -> {
                    String img = variant.getImageUrl() != null ? variant.getImageUrl() : (variant.getProduct() != null ? variant.getProduct().getImageUrl() : null);
                    detail.setImageUrl(img);
                });
            }
        }
    }
}