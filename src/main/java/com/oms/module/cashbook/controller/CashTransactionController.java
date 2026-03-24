package com.oms.module.cashbook.controller;

import com.oms.module.cashbook.dto.CashTransactionRequest;
import com.oms.module.cashbook.entity.CashTransaction;
import com.oms.module.cashbook.service.CashTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cashbook")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CashTransactionController {

    private final CashTransactionService cashTransactionService;

    @GetMapping
    public ResponseEntity<List<CashTransaction>> getAllTransactions() {
        return ResponseEntity.ok(cashTransactionService.getAllTransactions());
    }

    @GetMapping("/{voucherCode}")
    public ResponseEntity<CashTransaction> getTransactionByCode(@PathVariable String voucherCode) {
        return ResponseEntity.ok(cashTransactionService.getTransactionByCode(voucherCode));
    }

    @PostMapping
    public ResponseEntity<CashTransaction> createTransaction(@Valid @RequestBody CashTransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cashTransactionService.createTransaction(request));
    }

    @DeleteMapping("/{voucherCode}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable String voucherCode) {
        cashTransactionService.deleteTransaction(voucherCode);
        return ResponseEntity.noContent().build();
    }
}