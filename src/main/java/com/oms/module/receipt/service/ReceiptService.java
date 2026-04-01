package com.oms.module.receipt.service;

import com.oms.module.inventory.entity.Inventory;
import com.oms.module.inventory.repository.InventoryRepository;
import com.oms.module.product.entity.ProductVariant;
import com.oms.module.product.repository.ProductVariantRepository;
import com.oms.module.receipt.dto.ReceiptRequest;
import com.oms.module.receipt.dto.SupplierStatsResponse;
import com.oms.module.receipt.entity.Receipt;
import com.oms.module.receipt.entity.ReceiptActivity;
import com.oms.module.receipt.entity.ReceiptDetail;
import com.oms.module.receipt.repository.ReceiptActivityRepository;
import com.oms.module.receipt.repository.ReceiptRepository;
import com.oms.module.setting.repository.BranchRepository;
import com.oms.module.supplier.entity.Supplier;
import com.oms.module.supplier.repository.SupplierRepository;
import com.oms.module.account.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReceiptService {
    private final ReceiptRepository receiptRepository;
    private final SupplierRepository supplierRepository;
    private final ProductVariantRepository variantRepository;
    private final ReceiptActivityRepository activityRepository;
    private final InventoryRepository inventoryRepository;
    private final BranchRepository branchRepository;

    private String getCurrentUserName() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User) {
            return ((User) principal).getFullName();
        }
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @Transactional
    public Receipt createReceipt(ReceiptRequest request) {
        Supplier supplier = supplierRepository.findByCode(request.getSupplierCode())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy NCC"));

        String currentWorker = getCurrentUserName();
        String actualBranchName = "Kho không xác định";
        if (request.getBranchId() != null) {
            actualBranchName = branchRepository.findById(request.getBranchId())
                    .map(b -> b.getName())
                    .orElse("Kho mặc định");
        }
        Receipt receipt = Receipt.builder()
                .code("REI" + System.currentTimeMillis())
                .supplier(supplier)
                .branchId(request.getBranchId())
                .branchName(actualBranchName)
                .note(request.getNote())
                .itemsAmount(request.getItemsAmount())
                .discount(request.getDiscount())
                .shippingFee(request.getShippingFee())
                .totalAmount(request.getTotalAmount())
                .amountPaid(request.getAmountPaid())
                .paymentStatus(request.getPaymentStatus())
                .status("TRADING")
                .importStatus("PENDING")
                .creatorName(currentWorker)
                .build();

        List<ReceiptDetail> details = request.getItems().stream().map(item ->
                ReceiptDetail.builder()
                        .receipt(receipt)
                        .sku(item.getSku())
                        .quantity(item.getQuantity())
                        .importPrice(item.getImportPrice())
                        .warrantyMonths(item.getWarrantyMonths() != null ? item.getWarrantyMonths() : 0)
                        .build()
        ).collect(Collectors.toList());

        receipt.setDetails(details);

        Receipt savedReceipt = receiptRepository.save(receipt);
        logActivity(savedReceipt, "Tạo mới phiếu nhập hàng", currentWorker);

        if (Boolean.TRUE.equals(request.getIsImportStock())) {
            this.confirmImport(savedReceipt.getId());
        }

        return savedReceipt;
    }

    @Transactional
    public Receipt confirmImport(Long id) {
        Receipt receipt = receiptRepository.findById(id).orElseThrow();
        if ("COMPLETED".equals(receipt.getImportStatus())) {
            throw new RuntimeException("Đơn này đã nhập kho rồi!");
        }

        String currentWorker = getCurrentUserName();
        BigDecimal ratio = calculateExtraCostRatio(receipt);

        for (ReceiptDetail detail : receipt.getDetails()) {
            ProductVariant variant = variantRepository.findBySku(detail.getSku())
                    .orElseThrow(() -> new RuntimeException("Không thấy SP mã: " + detail.getSku()));

            BigDecimal extraCostPerItem = detail.getImportPrice().multiply(ratio);
            BigDecimal actualImportPrice = detail.getImportPrice().add(extraCostPerItem);

            updateMAC(variant, detail.getQuantity(), actualImportPrice);

            updateBranchInventory(receipt.getBranchId(), variant.getId(), detail.getQuantity());

            variant.setStockQuantity(variant.getStockQuantity() + detail.getQuantity());
            variantRepository.save(variant);
        }

        receipt.setImportStatus("COMPLETED");
        logActivity(receipt, "Xác nhận nhập kho vào: " + receipt.getBranchName(), currentWorker);

        checkAndCompleteReceipt(receipt);
        return receiptRepository.save(receipt);
    }

    private void updateBranchInventory(Long branchId, Long variantId, Integer qty) {
        Inventory inv = inventoryRepository.findByVariantIdAndBranchId(variantId, branchId)
                .orElse(Inventory.builder()
                        .variantId(variantId)
                        .branchId(branchId)
                        .stock(0)
                        .availableStock(0)
                        .build());

        inv.setStock(inv.getStock() + qty);
        inv.setAvailableStock(inv.getAvailableStock() + qty);
        inventoryRepository.save(inv);
    }

    private void updateMAC(ProductVariant variant, int importQty, BigDecimal actualPrice) {
        int oldStock = variant.getStockQuantity();
        BigDecimal oldCost = variant.getCostPrice() != null ? variant.getCostPrice() : BigDecimal.ZERO;

        if (oldStock + importQty > 0) {
            BigDecimal totalOldValue = oldCost.multiply(new BigDecimal(Math.max(0, oldStock)));
            BigDecimal totalNewValue = actualPrice.multiply(new BigDecimal(importQty));

            BigDecimal newCost = totalOldValue.add(totalNewValue)
                    .divide(new BigDecimal(oldStock + importQty), 2, RoundingMode.HALF_UP);

            variant.setCostPrice(newCost);
        }
    }

    private BigDecimal calculateExtraCostRatio(Receipt receipt) {
        if (receipt.getItemsAmount() == null || receipt.getItemsAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal fee = receipt.getShippingFee() != null ? receipt.getShippingFee() : BigDecimal.ZERO;
        BigDecimal discount = receipt.getDiscount() != null ? receipt.getDiscount() : BigDecimal.ZERO;
        return fee.subtract(discount).divide(receipt.getItemsAmount(), 4, RoundingMode.HALF_UP);
    }

    @Transactional
    public void addPayment(Long id, BigDecimal amountToAdd, String method) {
        Receipt receipt = receiptRepository.findById(id).orElseThrow();
        String currentWorker = getCurrentUserName();

        BigDecimal currentPaid = receipt.getAmountPaid() != null ? receipt.getAmountPaid() : BigDecimal.ZERO;
        BigDecimal totalAmount = receipt.getTotalAmount() != null ? receipt.getTotalAmount() : BigDecimal.ZERO;

        if (amountToAdd.compareTo(BigDecimal.ZERO) <= 0) throw new RuntimeException("Số tiền phải > 0");

        BigDecimal newPaidAmount = currentPaid.add(amountToAdd);
        receipt.setAmountPaid(newPaidAmount);

        if (newPaidAmount.compareTo(totalAmount) >= 0) {
            receipt.setPaymentStatus("PAID");
            if ("COMPLETED".equals(receipt.getImportStatus())) receipt.setStatus("COMPLETED");
        } else {
            receipt.setPaymentStatus("PARTIAL");
        }

        receiptRepository.save(receipt);
        logActivity(receipt, "Thanh toán: " + amountToAdd + "đ (" + method + ")", currentWorker);
    }

    private void checkAndCompleteReceipt(Receipt receipt) {
        if ("COMPLETED".equals(receipt.getImportStatus()) && "PAID".equals(receipt.getPaymentStatus())) {
            receipt.setStatus("COMPLETED");
        }
    }

    @Transactional
    public void cancelReceipt(Long id) {
        Receipt receipt = receiptRepository.findById(id).orElseThrow();
        if ("COMPLETED".equals(receipt.getImportStatus())) throw new RuntimeException("Không thể hủy đơn đã nhập kho");
        receipt.setStatus("CANCELLED");
        receiptRepository.save(receipt);
        logActivity(receipt, "Hủy phiếu nhập hàng", getCurrentUserName());
    }

    public SupplierStatsResponse getSupplierStats(String code, LocalDateTime start, LocalDateTime end) {
        List<Receipt> receipts;
        BigDecimal totalDebt;
        long totalOrders;
        BigDecimal totalAmount;

        // KIỂM TRA ĐIỀU KIỆN "TẤT CẢ THỜI GIAN"
        if (start == null || end == null) {
            // Lấy toàn bộ từ trước đến nay
            receipts = receiptRepository.findBySupplierCodeOrderByCreatedAtDesc(code);
            totalDebt = receiptRepository.getTotalDebtAllTime(code);

            // Tính toán tổng đơn và tổng tiền từ danh sách receipts
            totalOrders = receipts.size();
            totalAmount = receipts.stream()
                    .map(r -> r.getTotalAmount() != null ? r.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } else {
            // Lấy theo khoảng ngày (Logic cũ của anh)
            receipts = receiptRepository.findBySupplierCodeAndCreatedAtBetweenOrderByCreatedAtDesc(code, start, end);
            totalDebt = receiptRepository.getTotalDebt(code, start, end);

            // ... (Logic đếm đơn/tổng tiền theo kỳ) ...
            totalOrders = receipts.size(); // Hoặc dùng query count riêng
            totalAmount = receipts.stream()
                    .map(r -> r.getTotalAmount() != null ? r.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        // Map sang DTO trả về cho Frontend
        List<SupplierStatsResponse.ReceiptSummary> history = receipts.stream()
                .map(r -> SupplierStatsResponse.ReceiptSummary.builder()
                        .code(r.getCode())
                        .createdAt(r.getCreatedAt())
                        .status(r.getStatus())
                        .paymentStatus(r.getPaymentStatus())
                        .totalAmount(r.getTotalAmount())
                        .build())
                .collect(Collectors.toList());

        return SupplierStatsResponse.builder()
                .totalOrders(totalOrders)
                .totalAmount(totalAmount)
                .totalDebt(totalDebt != null ? totalDebt : BigDecimal.ZERO)
                .history(history)
                .build();
    }

    @Transactional
    public Receipt updateReceipt(String code, ReceiptRequest request) {
        Receipt receipt = receiptRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu mã: " + code));

        if ("COMPLETED".equals(receipt.getStatus()) || "COMPLETED".equals(receipt.getImportStatus())) {
            throw new RuntimeException("Đã nhập kho hoặc hoàn thành không được sửa!");
        }

        Supplier supplier = supplierRepository.findByCode(request.getSupplierCode())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy NCC"));

        receipt.setSupplier(supplier);
        receipt.setBranchId(request.getBranchId()); // Cập nhật ID chi nhánh
        receipt.setBranchName(request.getBranchName());
        receipt.setNote(request.getNote());
        receipt.setItemsAmount(request.getItemsAmount());
        receipt.setDiscount(request.getDiscount());
        receipt.setShippingFee(request.getShippingFee());
        receipt.setTotalAmount(request.getTotalAmount());

        BigDecimal paid = receipt.getAmountPaid() != null ? receipt.getAmountPaid() : BigDecimal.ZERO;
        if (paid.compareTo(receipt.getTotalAmount()) >= 0) receipt.setPaymentStatus("PAID");
        else if (paid.compareTo(BigDecimal.ZERO) > 0) receipt.setPaymentStatus("PARTIAL");
        else receipt.setPaymentStatus("UNPAID");

        receipt.getDetails().clear();
        List<ReceiptDetail> newDetails = request.getItems().stream().map(item ->
                ReceiptDetail.builder()
                        .receipt(receipt)
                        .sku(item.getSku())
                        .quantity(item.getQuantity())
                        .importPrice(item.getImportPrice())
                        .warrantyMonths(item.getWarrantyMonths() != null ? item.getWarrantyMonths() : 0)
                        .build()
        ).collect(Collectors.toList());

        receipt.getDetails().addAll(newDetails);
        logActivity(receipt, "Cập nhật thông tin phiếu nhập", getCurrentUserName());
        return receiptRepository.save(receipt);
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

    public Receipt getReceiptById(Long id) {
        return receiptRepository.findById(id).orElseThrow();
    }

    public Receipt getReceiptByCode(String code) {
        return receiptRepository.findByCode(code).orElseThrow();
    }
}