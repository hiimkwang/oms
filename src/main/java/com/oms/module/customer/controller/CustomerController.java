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

@RestController
@RequestMapping("/api/v1/customers")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @GetMapping("/{customerCode}")
    public ResponseEntity<Customer> getCustomerByCode(@PathVariable String customerCode) {
        return ResponseEntity.ok(customerService.getCustomerByCode(customerCode));
    }

    @PostMapping
    public ResponseEntity<Customer> createCustomer(@Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.createCustomer(request));
    }

    @PutMapping("/{customerCode}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable String customerCode, @Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(customerService.updateCustomer(customerCode, request));
    }

    @DeleteMapping("/{customerCode}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable String customerCode) {
        customerService.deleteCustomer(customerCode);
        return ResponseEntity.noContent().build();
    }
}