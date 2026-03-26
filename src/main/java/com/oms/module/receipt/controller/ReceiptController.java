package com.oms.module.receipt.controller;

import com.oms.module.receipt.dto.ReceiptRequest;
import com.oms.module.receipt.dto.SupplierStatsResponse;
import com.oms.module.receipt.entity.Receipt;
import com.oms.module.receipt.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/receipt")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ReceiptController {
    private final ReceiptService receiptService;

    @PostMapping
    public ResponseEntity<Receipt> create(@RequestBody ReceiptRequest request) {
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

    // Nút Xác nhận thanh toán
    @PostMapping("/{id}/payment")
    public ResponseEntity<?> makePayment(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        try {
            // Lấy số tiền trả thêm từ payload
            BigDecimal amountToAdd = new BigDecimal(payload.get("amountPaid").toString());
            String method = payload.get("paymentMethod").toString();

            // Gọi Service để cộng dồn tiền và cập nhật trạng thái (PAID hoặc PARTIAL)
            receiptService.addPayment(id, amountToAdd, method);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    // Nút Hủy đơn
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id) {
        receiptService.cancelReceipt(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/v1/suppliers/{code}/stats")
    public ResponseEntity<SupplierStatsResponse> getSupplierStats(
            @PathVariable String code,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        SupplierStatsResponse stats = receiptService.getSupplierStats(code, start, end);
        return ResponseEntity.ok(stats);
    }
    @PutMapping("/{code}")
    public ResponseEntity<?> updateReceipt(@PathVariable String code, @RequestBody ReceiptRequest request) {
        try {
            Receipt receipt = receiptService.updateReceipt(code, request);
            return ResponseEntity.ok(receipt);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}