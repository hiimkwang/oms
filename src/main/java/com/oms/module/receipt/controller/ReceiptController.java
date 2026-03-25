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
}