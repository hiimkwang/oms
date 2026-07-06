package com.oms.module.customer.controller;

import com.oms.module.customer.dto.CustomerRequest;
import com.oms.module.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    // Lấy danh sách khách hàng (phân trang + lọc keyword/nhóm phía BACKEND)
    @GetMapping
    public ResponseEntity<Map<String, Object>> getList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String group,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        var pageable = org.springframework.data.domain.PageRequest.of(
                Math.max(0, page), size <= 0 ? 20 : size,
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));

        org.springframework.data.domain.Page<CustomerRequest> pageData =
                customerService.getCustomerPage(keyword, group, pageable);

        Map<String, Object> body = new java.util.HashMap<>();
        body.put("content", pageData.getContent());
        body.put("totalElements", pageData.getTotalElements());
        body.put("totalPages", pageData.getTotalPages());
        body.put("page", pageData.getNumber());
        body.put("size", pageData.getSize());
        return ResponseEntity.ok(body);
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

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{code}")
    public ResponseEntity<?> delete(@PathVariable String code) {
        try {
            customerService.deleteCustomer(code);
            return ResponseEntity.ok(Map.of("message", "Xóa khách hàng thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // Bổ sung API xóa hàng loạt (chỉ ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/bulk-delete")
    public ResponseEntity<?> bulkDelete(@RequestBody List<String> codes) {
        try {
            customerService.bulkDeleteCustomers(codes);
            return ResponseEntity.ok(Map.of("message", "Đã xóa thành công " + codes.size() + " khách hàng!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/sync-groups")
    public ResponseEntity<?> syncAllCustomerGroups() {
        try {
            customerService.syncCustomerGroups();
            return ResponseEntity.ok(Map.of("message", "Đồng bộ thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}