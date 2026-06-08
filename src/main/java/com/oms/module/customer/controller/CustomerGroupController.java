package com.oms.module.customer.controller;

import com.oms.module.customer.entity.CustomerGroup;
import com.oms.module.customer.repository.CustomerGroupRepository;
import com.oms.module.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
            map.put("autoUpdate", g.getAutoUpdate() != null ? g.getAutoUpdate() : false);
            map.put("colorCode", g.getColorCode() != null ? g.getColorCode() : "#6c757d");
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<?> createGroup(@RequestBody CustomerGroup req) {
        // Chặn mass-assignment: không cho client tự gán id/createdAt khi tạo mới
        req.setId(null);
        req.setCreatedAt(null);
        if (req.getCode() == null || req.getCode().isBlank()) {
            req.setCode("N" + System.currentTimeMillis());
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
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> updateGroup(@PathVariable String code, @RequestBody CustomerGroup req) {
        try {
            CustomerGroup existing = groupRepository.findByCode(code)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhóm"));

            String oldName = existing.getName();
            String newName = req.getName();

            existing.setName(newName);
            existing.setNote(req.getNote());
            existing.setAutoUpdate(req.getAutoUpdate());
            existing.setConditions(req.getConditions());
            existing.setColorCode(req.getColorCode());
            CustomerGroup saved = groupRepository.save(existing);

            // Nếu đổi tên nhóm -> cập nhật lại tên nhóm cho toàn bộ khách hàng đang thuộc nhóm cũ
            // (tránh việc khách bị "rớt nhóm" âm thầm do liên kết theo chuỗi tên).
            if (oldName != null && newName != null && !oldName.equals(newName)) {
                customerRepository.renameCustomerGroup(oldName, newName);
            }
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // API Xóa hàng loạt nhóm khách hàng (chỉ ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
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