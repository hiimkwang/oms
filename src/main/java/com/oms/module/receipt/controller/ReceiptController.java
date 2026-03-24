package com.oms.module.receipt.controller;

import com.oms.module.receipt.dto.ReceiptRequest;
import com.oms.module.receipt.entity.Receipt;
import com.oms.module.receipt.service.ReceiptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/receipts")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ReceiptController {

    private final ReceiptService receiptService;

    @PostMapping
    public ResponseEntity<Receipt> createReceipt(@Valid @RequestBody ReceiptRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(receiptService.createReceipt(request));
    }

    @GetMapping("/{receiptCode}")
    public ResponseEntity<Receipt> getReceiptByCode(@PathVariable String receiptCode) {
        return ResponseEntity.ok(receiptService.getReceiptByCode(receiptCode));
    }
}