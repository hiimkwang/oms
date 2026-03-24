package com.oms.module.quotation.controller;

import com.oms.module.quotation.dto.QuotationRequest;
import com.oms.module.quotation.entity.Quotation;
import com.oms.module.quotation.service.QuotationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quotations")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class QuotationController {

    private final QuotationService quotationService;

    @GetMapping
    public ResponseEntity<List<Quotation>> getAllQuotations() {
        return ResponseEntity.ok(quotationService.getAllQuotations());
    }

    @GetMapping("/{quotationCode}")
    public ResponseEntity<Quotation> getQuotationByCode(@PathVariable String quotationCode) {
        return ResponseEntity.ok(quotationService.getQuotationByCode(quotationCode));
    }

    @PostMapping
    public ResponseEntity<Quotation> createQuotation(@Valid @RequestBody QuotationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(quotationService.createQuotation(request));
    }

    @PatchMapping("/{quotationCode}/status")
    public ResponseEntity<Quotation> updateStatus(
            @PathVariable String quotationCode,
            @RequestParam String status) {
        return ResponseEntity.ok(quotationService.updateQuotationStatus(quotationCode, status));
    }

    @DeleteMapping("/{quotationCode}")
    public ResponseEntity<Void> deleteQuotation(@PathVariable String quotationCode) {
        quotationService.deleteQuotation(quotationCode);
        return ResponseEntity.noContent().build();
    }
}