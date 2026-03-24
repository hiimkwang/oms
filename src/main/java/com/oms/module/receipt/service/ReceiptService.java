package com.oms.module.receipt.service;

import com.oms.module.product.entity.Product;
import com.oms.module.product.service.ProductService;
import com.oms.module.receipt.dto.ReceiptDetailRequest;
import com.oms.module.receipt.dto.ReceiptRequest;
import com.oms.module.receipt.entity.Receipt;
import com.oms.module.receipt.entity.ReceiptDetail;
import com.oms.module.receipt.repository.ReceiptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final ProductService productService;

    @Transactional
    public Receipt createReceipt(ReceiptRequest request) {
        if (receiptRepository.existsByReceiptCode(request.getReceiptCode())) {
            throw new RuntimeException("Số phiếu nhập đã tồn tại: " + request.getReceiptCode());
        }

        Receipt receipt = Receipt.builder()
                .receiptCode(request.getReceiptCode())
                .receiptDate(request.getReceiptDate())
                .supplierName(request.getSupplierName())
                .importer(request.getImporter())
                .note(request.getNote())
                .receiptDetails(new ArrayList<>())
                .build();

        double totalReceiptAmount = 0.0;

        for (ReceiptDetailRequest detailReq : request.getDetails()) {
            Product product = productService.getProductBySku(detailReq.getSku());

            double totalPrice = detailReq.getQuantity() * detailReq.getImportPrice();

            ReceiptDetail detail = ReceiptDetail.builder()
                    .receipt(receipt)
                    .product(product)
                    .quantity(detailReq.getQuantity())
                    .importPrice(detailReq.getImportPrice())
                    .totalPrice(totalPrice)
                    .build();

            receipt.getReceiptDetails().add(detail);
            totalReceiptAmount += totalPrice;

            // TODO: Tại đây bạn có thể gọi thêm hàm cập nhật số lượng tồn kho trong ProductService
            // Ví dụ: productService.addStock(product.getSku(), detailReq.getQuantity());
        }

        receipt.setTotalAmount(totalReceiptAmount);

        return receiptRepository.save(receipt);
    }

    public Receipt getReceiptByCode(String receiptCode) {
        return receiptRepository.findByReceiptCode(receiptCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập: " + receiptCode));
    }
}