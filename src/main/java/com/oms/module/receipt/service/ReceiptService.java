package com.oms.module.receipt.service;

import com.oms.module.product.entity.ProductVariant;
import com.oms.module.product.repository.ProductVariantRepository;
import com.oms.module.receipt.dto.ReceiptRequest;
import com.oms.module.receipt.entity.Receipt;
import com.oms.module.receipt.entity.ReceiptDetail;
import com.oms.module.receipt.repository.ReceiptRepository;
import com.oms.module.supplier.entity.Supplier;
import com.oms.module.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReceiptService {
    private final ReceiptRepository receiptRepository;
    private final SupplierRepository supplierRepository;
    private final ProductVariantRepository variantRepository;

    @Transactional
    public Receipt createReceipt(ReceiptRequest request) {
        Supplier supplier = supplierRepository.findByCode(request.getSupplierCode())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy NCC"));

        // 1. Tạo đối tượng Receipt
        Receipt receipt = Receipt.builder()
                .code("REI" + System.currentTimeMillis()) // Logic sinh mã đơn
                .supplier(supplier)
                .branchName(request.getBranchName())
                .totalAmount(request.getTotalAmount())
                .note(request.getNote())
                .paymentStatus(request.getPaymentStatus())
                .creatorName("QUANG BUI") // Tạm thời để cứng hoặc lấy từ Security
                .build();

        // 2. Tạo danh sách chi tiết và CẬP NHẬT KHO
        List<ReceiptDetail> details = request.getItems().stream().map(item -> {
            // Tìm variant trong kho
            ProductVariant variant = variantRepository.findBySku(item.getSku())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy SKU: " + item.getSku()));

            // CỘNG TỒN KHO
            variant.setStockQuantity(variant.getStockQuantity() + item.getQuantity());
            // Cập nhật luôn giá vốn (Giá nhập mới nhất)
            variant.setCostPrice(item.getImportPrice());
            variantRepository.save(variant);

            return ReceiptDetail.builder()
                    .receipt(receipt)
                    .sku(item.getSku())
                    .productName(variant.getProduct().getName())
                    .quantity(item.getQuantity())
                    .importPrice(item.getImportPrice())
                    .build();
        }).collect(Collectors.toList());

        receipt.setDetails(details);
        receipt.setTotalQuantity(details.stream().mapToInt(ReceiptDetail::getQuantity).sum());

        return receiptRepository.save(receipt);
    }

    public List<Receipt> getAllReceipts() {
        return receiptRepository.findAll();
    }
    public Receipt getReceiptById(Long id) {
        return receiptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Đơn nhập hàng không tồn tại!"));
    }
}