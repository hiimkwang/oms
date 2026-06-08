package com.oms.module.cashbook.controller;

import com.oms.module.account.repository.UserRepository;
import com.oms.module.cashbook.dto.CashTransactionRequest;
import com.oms.module.cashbook.service.CashbookService;
import com.oms.module.customer.repository.CustomerRepository;
import com.oms.module.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/cashbook")
@RequiredArgsConstructor
public class CashTransactionController {

    private final CashbookService cashbookService;
    private final CustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> create(@jakarta.validation.Valid @RequestBody CashTransactionRequest request) {
        return ResponseEntity.ok(cashbookService.createTransaction(request));
    }

    @GetMapping("/search-employee")
    public List<?> searchEmployee(@RequestParam String query) {
        return userRepository.findByFullNameContainingIgnoreCaseOrUsernameContaining(query, query);
    }

    @GetMapping("/search-customer")
    public List<?> searchCustomer(@RequestParam String query) {
        return customerRepository.findByFullNameContainingIgnoreCaseOrPhoneContaining(query, query);
    }

    @GetMapping("/search-supplier")
    public List<?> searchSupplier(@RequestParam String query) {
        return supplierRepository.findByNameContainingIgnoreCaseOrCodeContaining(query, query);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String desc = payload.get("description");
        String attachments = payload.get("attachments"); // Dạng JSON string: "['url1', 'url2']"
        return ResponseEntity.ok(cashbookService.updateDetails(id, desc, attachments));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        cashbookService.deleteTransaction(id);
        return ResponseEntity.ok("Đã xóa");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/bulk")
    public ResponseEntity<?> deleteBulk(@RequestBody List<Long> ids) {
        for (Long id : ids) {
            cashbookService.deleteTransaction(id);
        }
        return ResponseEntity.ok("Xóa thành công");
    }
}