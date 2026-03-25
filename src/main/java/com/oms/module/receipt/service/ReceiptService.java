package com.oms.module.receipt.service;

import com.oms.module.product.entity.ProductVariant;
import com.oms.module.product.repository.ProductVariantRepository;
import com.oms.module.receipt.dto.ReceiptRequest;
import com.oms.module.receipt.dto.SupplierStatsResponse;
import com.oms.module.receipt.entity.Receipt;
import com.oms.module.receipt.entity.ReceiptActivity;
import com.oms.module.receipt.entity.ReceiptDetail;
import com.oms.module.receipt.repository.ReceiptActivityRepository;
import com.oms.module.receipt.repository.ReceiptRepository;
import com.oms.module.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
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
    private final ReceiptActivityRepository receiptActivityRepository;


    @Transactional
    public Receipt createReceipt(ReceiptRequest request) {
        Receipt receipt = Receipt.builder()
                .code("REI" + System.currentTimeMillis())
                .supplier(supplierRepository.findByCode(request.getSupplierCode()).orElse(null))
                .branchName(request.getBranchName())
                .totalAmount(request.getTotalAmount())
                .paymentStatus("UNPAID")
                .status("TRADING") // Trạng thái: Đang giao dịch
                .importStatus("PENDING") // Trạng thái nhập: Chờ nhập kho
                .creatorName("QUANG BUI")
                .build();

        List<ReceiptDetail> details = request.getItems().stream().map(item ->
                ReceiptDetail.builder()
                        .receipt(receipt)
                        .sku(item.getSku())
                        .quantity(item.getQuantity())
                        .importPrice(item.getImportPrice())
                        .build()
        ).collect(Collectors.toList());

        receipt.setDetails(details);
        return receiptRepository.save(receipt);
    }

    @Transactional
    public Receipt confirmImport(Long id) {
        Receipt receipt = receiptRepository.findById(id).orElseThrow();
        if ("COMPLETED".equals(receipt.getImportStatus())) throw new RuntimeException("Đã nhập kho!");

        for (ReceiptDetail detail : receipt.getDetails()) {
            ProductVariant variant = variantRepository.findBySku(detail.getSku()).orElseThrow();

            int oldStock = variant.getStockQuantity();
            BigDecimal oldCost = variant.getCostPrice() != null ? variant.getCostPrice() : BigDecimal.ZERO;

            // Tính giá vốn bình quân (MAC)
            if (oldStock + detail.getQuantity() > 0) {
                BigDecimal totalOldValue = oldCost.multiply(new BigDecimal(oldStock));
                BigDecimal totalNewValue = detail.getImportPrice().multiply(new BigDecimal(detail.getQuantity()));
                BigDecimal newCost = totalOldValue.add(totalNewValue)
                        .divide(new BigDecimal(oldStock + detail.getQuantity()), 2, RoundingMode.HALF_UP);
                variant.setCostPrice(newCost);
            }

            variant.setStockQuantity(oldStock + detail.getQuantity());
            variantRepository.save(variant);
        }

        receipt.setImportStatus("COMPLETED"); // Chuyển trạng thái nhập kho
        saveActivity(receipt, "Nhập kho", "QUANG BUI"); // Ghi lịch sử

        // GỌI HÀM KIỂM TRA ĐỂ CHỐT ĐƠN (Thêm dòng này)
        checkAndCompleteReceipt(receipt);

        return receiptRepository.save(receipt);
    }

    @Transactional
    public Receipt confirmPayment(Long id) {
        Receipt receipt = receiptRepository.findById(id).orElseThrow();
        receipt.setPaymentStatus("PAID"); // Đổi trạng thái thanh toán
        saveActivity(receipt, "Xác nhận thanh toán", "QUANG BUI");

        // GỌI HÀM KIỂM TRA ĐỂ CHỐT ĐƠN (Thêm dòng này)
        checkAndCompleteReceipt(receipt);

        return receiptRepository.save(receipt);
    }

    // SỬA LẠI HÀM NÀY CHO CHUẨN VỚI TÊN BIẾN CỦA ÔNG
    private void checkAndCompleteReceipt(Receipt receipt) {
        // Kiểm tra: Nếu importStatus là "COMPLETED" (Đã nhập) VÀ paymentStatus là "PAID" (Đã trả tiền)
        if ("COMPLETED".equals(receipt.getImportStatus()) && "PAID".equals(receipt.getPaymentStatus())) {
            // Thì cập nhật trạng thái tổng (status) thành COMPLETED (Hoàn thành)
            receipt.setStatus("COMPLETED");
            saveActivity(receipt, "Hoàn thành đơn nhập hàng", "Hệ thống");
        }
    }

    @Transactional
    public void cancelReceipt(Long id) {
        Receipt receipt = receiptRepository.findById(id).orElseThrow();
        if ("COMPLETED".equals(receipt.getImportStatus())) throw new RuntimeException("Không thể hủy đơn đã nhập kho");
        receipt.setStatus("CANCELLED");
        saveActivity(receipt, "Hủy đơn hàng", "QUANG BUI");
        receiptRepository.save(receipt);
    }

    private void saveActivity(Receipt receipt, String action, String creator) {
        ReceiptActivity activity = ReceiptActivity.builder()
                .receipt(receipt)
                .action(action)
                .creatorName(creator)
                .createdAt(LocalDateTime.now())
                .build();
        receiptActivityRepository.save(activity);
    }

    public List<Receipt> getAllReceipts() {
        return receiptRepository.findAll();
    }

    public Receipt getReceiptById(Long id) {
        return receiptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Đơn nhập hàng không tồn tại!"));
    }

    public SupplierStatsResponse getSupplierStats(String code, LocalDateTime start, LocalDateTime end) {
        // 1. Lấy thống kê cơ bản (Count và Sum)
        Object[] basicStats = receiptRepository.getBasicStats(code, start, end);
        Object[] statsResult = (Object[]) basicStats[0]; // Vì Query trả về mảng object

        long count = (long) (statsResult[0] != null ? statsResult[0] : 0L);
        BigDecimal total = (BigDecimal) (statsResult[1] != null ? statsResult[1] : BigDecimal.ZERO);

        // 2. Lấy nợ
        BigDecimal debt = receiptRepository.getTotalDebt(code, start, end);
        if (debt == null) debt = BigDecimal.ZERO;

        // 3. Lấy danh sách lịch sử và convert sang DTO
        List<Receipt> receipts = receiptRepository.findBySupplierCodeAndCreatedAtBetweenOrderByCreatedAtDesc(code, start, end);
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
                .totalOrders(count)
                .totalAmount(total)
                .totalDebt(debt)
                .history(history)
                .build();
    }
}