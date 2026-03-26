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
import com.oms.module.supplier.entity.Supplier;
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
    private final ReceiptActivityRepository activityRepository;

    @Transactional
    public Receipt createReceipt(ReceiptRequest request) {
        Supplier supplier = supplierRepository.findByCode(request.getSupplierCode())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy NCC"));

        Receipt receipt = Receipt.builder()
                .code("REI" + System.currentTimeMillis())
                .supplier(supplier)
                .branchName(request.getBranchName())
                .note(request.getNote())
                .itemsAmount(request.getItemsAmount())
                .discount(request.getDiscount())
                .shippingFee(request.getShippingFee())
                .totalAmount(request.getTotalAmount())
                .amountPaid(request.getAmountPaid())
                .paymentStatus(request.getPaymentStatus())
                .status("TRADING")
                .importStatus("PENDING")
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

        Receipt savedReceipt = receiptRepository.save(receipt);
        logActivity(savedReceipt, "Tạo mới phiếu nhập hàng", "Hệ thống");
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

        BigDecimal ratio = BigDecimal.ZERO;
        // Kiểm tra xem tiền hàng có > 0 không để tránh lỗi chia cho 0
        if (receipt.getItemsAmount() != null && receipt.getItemsAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal fee = receipt.getShippingFee() != null ? receipt.getShippingFee() : BigDecimal.ZERO;
            BigDecimal discount = receipt.getDiscount() != null ? receipt.getDiscount() : BigDecimal.ZERO;

            // Công thức: (Phí - Giảm giá) / Tổng tiền hàng
            BigDecimal netExtra = fee.subtract(discount);
            ratio = netExtra.divide(receipt.getItemsAmount(), 4, RoundingMode.HALF_UP);
        }

        // Duyệt qua từng sản phẩm để cộng tồn và tính lại giá vốn MAC
        for (ReceiptDetail detail : receipt.getDetails()) {
            ProductVariant variant = variantRepository.findBySku(detail.getSku()).orElseThrow();

            // 1. Tính giá nhập thực tế (Đã gánh thêm phí ship hoặc được trừ giảm giá)
            // Actual Price = Import Price + (Import Price * ratio)
            BigDecimal extraCostPerItem = detail.getImportPrice().multiply(ratio);
            BigDecimal actualImportPrice = detail.getImportPrice().add(extraCostPerItem);

            // 2. Lấy tồn kho và giá vốn cũ
            int oldStock = variant.getStockQuantity();
            BigDecimal oldCost = variant.getCostPrice() != null ? variant.getCostPrice() : BigDecimal.ZERO;

            // 3. Tính giá vốn bình quân (MAC) với Actual Price
            if (oldStock + detail.getQuantity() > 0) {
                BigDecimal totalOldValue = oldCost.multiply(new BigDecimal(oldStock));
                BigDecimal totalNewValue = actualImportPrice.multiply(new BigDecimal(detail.getQuantity()));

                BigDecimal newCost = totalOldValue.add(totalNewValue)
                        .divide(new BigDecimal(oldStock + detail.getQuantity()), 2, RoundingMode.HALF_UP);

                variant.setCostPrice(newCost);
            }
            logActivity(receipt, "Xác nhận nhập kho hàng hóa", "Quang");
            // 4. Cộng tồn kho
            variant.setStockQuantity(oldStock + detail.getQuantity());
            variantRepository.save(variant);
        }

        receipt.setImportStatus("COMPLETED");

        // Gọi hàm kiểm tra nếu đã thanh toán xong thì chốt luôn đơn thành COMPLETED
        checkAndCompleteReceipt(receipt);

        return receiptRepository.save(receipt);
    }

//    @Transactional
//    public Receipt confirmPayment(Long id) {
//        Receipt receipt = receiptRepository.findById(id).orElseThrow();
//        receipt.setPaymentStatus("PAID"); // Đổi trạng thái thanh toán
//        saveActivity(receipt, "Xác nhận thanh toán", "QUANG BUI");
//
//        // GỌI HÀM KIỂM TRA ĐỂ CHỐT ĐƠN (Thêm dòng này)
//        checkAndCompleteReceipt(receipt);
//
//        return receiptRepository.save(receipt);
//    }

    @Transactional
    public void addPayment(Long id, BigDecimal amountToAdd, String method) {
        Receipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Phiếu nhập với ID: " + id));

        if ("CANCELLED".equals(receipt.getStatus())) {
            throw new RuntimeException("Đơn hàng đã hủy, không thể thanh toán!");
        }
        if ("PAID".equals(receipt.getPaymentStatus())) {
            throw new RuntimeException("Đơn hàng này đã được thanh toán đủ từ trước!");
        }

        BigDecimal currentPaid = receipt.getAmountPaid() != null ? receipt.getAmountPaid() : BigDecimal.ZERO;
        BigDecimal totalAmount = receipt.getTotalAmount() != null ? receipt.getTotalAmount() : BigDecimal.ZERO;

        BigDecimal remainingDebt = totalAmount.subtract(currentPaid);

        if (amountToAdd.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Số tiền thanh toán phải lớn hơn 0!");
        }
        if (amountToAdd.compareTo(remainingDebt) > 0) {
            throw new RuntimeException("Số tiền trả (" + amountToAdd + ") lớn hơn số nợ hiện tại (" + remainingDebt + ")!");
        }

        BigDecimal newPaidAmount = currentPaid.add(amountToAdd);
        receipt.setAmountPaid(newPaidAmount);

        if (newPaidAmount.compareTo(totalAmount) >= 0) {
            receipt.setPaymentStatus("PAID");

            if ("COMPLETED".equals(receipt.getImportStatus())) {
                receipt.setStatus("COMPLETED");
            }
        } else {
            receipt.setPaymentStatus("PARTIAL");
        }

        ReceiptActivity activity = new ReceiptActivity();
        activity.setReceipt(receipt);
        activity.setCreatorName("QUANG BUI");
        activity.setAction("Thanh toán nợ: " + amountToAdd + "đ (" + method + ")");
        activityRepository.save(activity);


        // 8. Lưu phiếu nhập
        receiptRepository.save(receipt);
        logActivity(receipt, "Thanh toán cho NCC: " + amountToAdd + "đ (Hình thức: " + method + ")", "Quang");
    }

    private void checkAndCompleteReceipt(Receipt receipt) {
        // Kiểm tra: Nếu importStatus là "COMPLETED" (Đã nhập) VÀ paymentStatus là "PAID" (Đã trả tiền)
        if ("COMPLETED".equals(receipt.getImportStatus()) && "PAID".equals(receipt.getPaymentStatus())) {
            // Thì cập nhật trạng thái tổng (status) thành COMPLETED (Hoàn thành)
            receipt.setStatus("COMPLETED");
            logActivity(receipt, "Hoàn thành đơn nhập hàng", "Quang");
        }
    }

    @Transactional
    public void cancelReceipt(Long id) {
        Receipt receipt = receiptRepository.findById(id).orElseThrow();
        if ("COMPLETED".equals(receipt.getImportStatus())) throw new RuntimeException("Không thể hủy đơn đã nhập kho");
        receipt.setStatus("CANCELLED");
        receiptRepository.save(receipt);
        logActivity(receipt, "Hủy phiếu nhập hàng", "Quang");
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
    public Receipt getReceiptByCode(String code) {
        return receiptRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Phiếu nhập với mã: " + code));
    }
    @Transactional
    public Receipt updateReceipt(String code, ReceiptRequest request) {
        // 1. Tìm đơn cũ bằng CODE
        Receipt receipt = receiptRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập mã: " + code));

        // 2. Chặn sửa nếu đơn đã hoàn thành hoặc đã nhập kho
        if ("COMPLETED".equals(receipt.getStatus()) || "COMPLETED".equals(receipt.getImportStatus())) {
            throw new RuntimeException("Không thể sửa đơn đã hoàn thành hoặc đã nhập kho!");
        }

        // 3. Đổi thông tin cơ bản
        Supplier supplier = supplierRepository.findByCode(request.getSupplierCode())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy NCC"));
        receipt.setSupplier(supplier);
        receipt.setBranchName(request.getBranchName());
        receipt.setNote(request.getNote());

        // 4. Cập nhật Tiền nong
        receipt.setItemsAmount(request.getItemsAmount());
        receipt.setDiscount(request.getDiscount());
        receipt.setShippingFee(request.getShippingFee());
        receipt.setTotalAmount(request.getTotalAmount());

        // 5. Cập nhật lại Trạng thái thanh toán (Cực kỳ quan trọng)
        // Vì tổng tiền (totalAmount) vừa thay đổi, ta phải so sánh lại với tiền đã trả (amountPaid)
        BigDecimal paid = receipt.getAmountPaid() != null ? receipt.getAmountPaid() : BigDecimal.ZERO;
        if (paid.compareTo(receipt.getTotalAmount()) >= 0) {
            receipt.setPaymentStatus("PAID"); // Đã trả đủ
        } else if (paid.compareTo(BigDecimal.ZERO) > 0) {
            receipt.setPaymentStatus("PARTIAL"); // Đã trả một phần
        } else {
            receipt.setPaymentStatus("UNPAID"); // Chưa trả đồng nào
        }

        // 6. Cập nhật Giỏ hàng (Xóa sạch đồ cũ, nhét đồ mới vào)
        // Yêu cầu Entity Receipt.java phải cấu hình:
        // @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
        receipt.getDetails().clear(); // Xóa sạch List cũ

        List<ReceiptDetail> newDetails = request.getItems().stream().map(item ->
                ReceiptDetail.builder()
                        .receipt(receipt)
                        .sku(item.getSku())
                        .quantity(item.getQuantity())
                        .importPrice(item.getImportPrice())
                        .build()
        ).collect(Collectors.toList());

        receipt.getDetails().addAll(newDetails); // Nhét List mới vào
        logActivity(receipt, "Cập nhật thông tin và giỏ hàng", "Quang");
        return receiptRepository.save(receipt);
    }

    private void logActivity(Receipt receipt, String action, String creator) {
        ReceiptActivity activity = new ReceiptActivity();
        activity.setReceipt(receipt);
        // Tạm thời fix cứng tên ông, sau này có Login thì lấy từ SecurityContextHolder ra nhé
        activity.setCreatorName(creator);
        activity.setAction(action);

        activityRepository.save(activity);
    }
}