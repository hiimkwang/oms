package com.oms.module.cashbook.service;

import com.oms.module.cashbook.dto.CashTransactionRequest;
import com.oms.module.cashbook.entity.CashTransaction;
import com.oms.module.cashbook.repository.CashTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CashTransactionService {

    private final CashTransactionRepository cashTransactionRepository;

    public List<CashTransaction> getAllTransactions() {
        return cashTransactionRepository.findAll();
    }

    public CashTransaction getTransactionByCode(String voucherCode) {
        return cashTransactionRepository.findByVoucherCode(voucherCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu thu/chi: " + voucherCode));
    }

    @Transactional
    public CashTransaction createTransaction(CashTransactionRequest request) {
        if (cashTransactionRepository.existsByVoucherCode(request.getVoucherCode())) {
            throw new RuntimeException("Số phiếu thu/chi đã tồn tại: " + request.getVoucherCode());
        }

        CashTransaction transaction = CashTransaction.builder()
                .voucherCode(request.getVoucherCode())
                .transactionType(request.getTransactionType())
                .transactionDate(request.getTransactionDate())
                .personName(request.getPersonName())
                .address(request.getAddress())
                .referenceDocument(request.getReferenceDocument())
                .description(request.getDescription())
                .amount(request.getAmount())
                .build();

        return cashTransactionRepository.save(transaction);
    }

    @Transactional
    public void deleteTransaction(String voucherCode) {
        CashTransaction transaction = getTransactionByCode(voucherCode);
        cashTransactionRepository.delete(transaction);
    }
}