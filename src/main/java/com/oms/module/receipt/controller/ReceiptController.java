package com.oms.module.receipt.controller;

import com.oms.module.receipt.dto.ReceiptRequest;
import com.oms.module.receipt.entity.Receipt;
import com.oms.module.receipt.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/receipt")
@RequiredArgsConstructor
public class ReceiptController {
    private final ReceiptService receiptService;

    @PostMapping
    public ResponseEntity<Receipt> create(@jakarta.validation.Valid @RequestBody ReceiptRequest request) {
        return ResponseEntity.ok(receiptService.createReceipt(request));
    }

    @GetMapping
    public ResponseEntity<List<Receipt>> getAll() {
        return ResponseEntity.ok(receiptService.getAllReceipts());
    }

    @PostMapping("/{id}/receive")
    public ResponseEntity<?> receiveStock(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(receiptService.confirmImport(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Hoàn tác nhập kho: đưa đơn ĐÃ NHẬP KHO về CHỜ NHẬP KHO (khi lỡ bấm nhập nhưng hàng chưa về)
    @PostMapping("/{id}/revert-import")
    public ResponseEntity<?> revertImport(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(receiptService.revertImport(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage() != null ? e.getMessage() : "Lỗi hoàn tác nhập kho"));
        }
    }

    @PostMapping("/{id}/payment")
    public ResponseEntity<?> makePayment(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        try {
            BigDecimal amountToAdd = new BigDecimal(payload.get("amountPaid").toString());
            String method = payload.get("paymentMethod").toString();

            receiptService.addPayment(id, amountToAdd, method);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id) {
        receiptService.cancelReceipt(id);
        return ResponseEntity.ok().build();
    }

    // Xóa phiếu nhập hàng loạt (FE màn nhập hàng gọi POST /api/v1/imports/bulk-delete)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/v1/imports/bulk-delete")
    public ResponseEntity<?> deleteImportsBulk(@RequestBody List<Long> ids) {
        receiptService.deleteBulk(ids);
        return ResponseEntity.ok(Map.of("message", "Đã xóa thành công các phiếu nhập đã chọn"));
    }

    // (Đã gỡ route trùng GET /api/v1/suppliers/{code}/stats — đã có ở SupplierController)

    @PutMapping("/{code}")
    public ResponseEntity<?> updateReceipt(@PathVariable String code, @jakarta.validation.Valid @RequestBody ReceiptRequest request) {
        try {
            Receipt receipt = receiptService.updateReceipt(code, request);
            return ResponseEntity.ok(receipt);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}