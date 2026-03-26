package com.oms.module.customer.controller;

import com.oms.module.customer.dto.CustomerRequest;
import com.oms.module.customer.entity.Customer;
import com.oms.module.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;

    @GetMapping
    public ResponseEntity<List<CustomerRequest>> getList(@RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(customerService.getCustomerList(keyword));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid CustomerRequest req) {
        try {
            return ResponseEntity.ok(customerService.createCustomer(req));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}