package com.oms.module.customer.controller;

import com.oms.module.customer.entity.CustomerGroup;
import com.oms.module.customer.repository.CustomerGroupRepository;
import com.oms.module.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/customer-groups")
@RequiredArgsConstructor
public class CustomerGroupController {

    private final CustomerGroupRepository groupRepository;
    private final CustomerRepository customerRepository;

    @GetMapping
    public ResponseEntity<?> getAllGroups() {
        List<Map<String, Object>> result = groupRepository.findAll().stream().map(g -> {
            long count = customerRepository.countByCustomerGroup(g.getName());

            Map<String, Object> map = new HashMap<>();
            map.put("code", g.getCode() != null ? g.getCode() : "");
            map.put("name", g.getName() != null ? g.getName() : "");
            map.put("note", g.getNote() != null ? g.getNote() : "");
            map.put("customerCount", count);
            map.put("createdAt", g.getCreatedAt());

            // BỔ SUNG DÒNG NÀY: Trả về trạng thái tự động hay thủ công
            map.put("autoUpdate", g.getAutoUpdate() != null ? g.getAutoUpdate() : false);

            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<?> createGroup(@RequestBody CustomerGroup req) {
        if (req.getCode() == null || req.getCode().isBlank()) {
            req.setCode("N" + System.currentTimeMillis()); // Tự sinh mã nếu để trống
        }
        return ResponseEntity.ok(groupRepository.save(req));
    }

    // API Lấy chi tiết nhóm
    @GetMapping("/{code}")
    public ResponseEntity<?> getGroupByCode(@PathVariable String code) {
        try {
            return ResponseEntity.ok(groupRepository.findByCode(code).orElseThrow(() -> new RuntimeException("Không tìm thấy nhóm")));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // API Cập nhật nhóm
    @PutMapping("/{code}")
    public ResponseEntity<?> updateGroup(@PathVariable String code, @RequestBody CustomerGroup req) {
        try {
            CustomerGroup existing = groupRepository.findByCode(code)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhóm"));
            existing.setName(req.getName());
            existing.setNote(req.getNote());
            existing.setAutoUpdate(req.getAutoUpdate());
            existing.setConditions(req.getConditions());
            return ResponseEntity.ok(groupRepository.save(existing));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // API Xóa hàng loạt nhóm khách hàng
    @PostMapping("/bulk-delete")
    public ResponseEntity<?> bulkDeleteGroups(@RequestBody List<String> codes) {
        try {
            if (codes != null && !codes.isEmpty()) {
                groupRepository.deleteAllByCodeIn(codes);
            }
            return ResponseEntity.ok(Map.of("message", "Đã xóa thành công " + codes.size() + " nhóm!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}