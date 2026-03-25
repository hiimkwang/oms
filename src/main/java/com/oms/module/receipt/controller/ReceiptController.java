package com.oms.module.receipt.controller;

import com.oms.module.receipt.dto.ReceiptRequest;
import com.oms.module.receipt.entity.Receipt;
import com.oms.module.receipt.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<?> pay(@PathVariable Long id) {
        return ResponseEntity.ok(receiptService.confirmPayment(id));
    }

    // Nút Hủy đơn
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id) {
        receiptService.cancelReceipt(id);
        return ResponseEntity.ok().build();
    }
}