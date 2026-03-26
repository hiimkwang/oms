package com.oms.module.customer.controller;

import com.oms.module.customer.dto.CustomerRequest;
import com.oms.module.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    // Lấy danh sách khách hàng (có hỗ trợ tìm kiếm theo keyword)
    @GetMapping
    public ResponseEntity<List<CustomerRequest>> getList(@RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(customerService.getCustomerList(keyword));
    }

    // Lấy chi tiết 1 khách hàng theo Mã (code)
    @GetMapping("/{code}")
    public ResponseEntity<?> getDetail(@PathVariable String code) {
        try {
            return ResponseEntity.ok(customerService.getCustomerByCode(code));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // Thêm mới khách hàng
    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid CustomerRequest req) {
        try {
            return ResponseEntity.ok(customerService.createCustomer(req));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // Cập nhật thông tin khách hàng
    @PutMapping("/{code}")
    public ResponseEntity<?> update(@PathVariable String code, @RequestBody @Valid CustomerRequest req) {
        try {
            return ResponseEntity.ok(customerService.updateCustomer(code, req));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<?> delete(@PathVariable String code) {
        try {
            customerService.deleteCustomer(code);
            // Trả về JSON thành công
            return ResponseEntity.ok(Map.of("message", "Xóa khách hàng thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    // Bổ sung API xóa hàng loạt
    @PostMapping("/bulk-delete")
    public ResponseEntity<?> bulkDelete(@RequestBody List<String> codes) {
        try {
            customerService.bulkDeleteCustomers(codes);
            return ResponseEntity.ok(Map.of("message", "Đã xóa thành công " + codes.size() + " khách hàng!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}