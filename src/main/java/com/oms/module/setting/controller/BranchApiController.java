package com.oms.module.setting.controller;

import com.oms.module.setting.entity.Branch;
import com.oms.module.setting.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/settings/branches")
@RequiredArgsConstructor
public class BranchApiController {

    private final BranchRepository branchRepository;

    // API Tạo mới chi nhánh
    @PostMapping
    public ResponseEntity<Branch> createBranch(@RequestBody Branch request) {
        request.setActive(true); // Mặc định tạo mới là đang hoạt động
        Branch saved = branchRepository.save(request);
        return ResponseEntity.ok(saved);
    }

    // API Cập nhật chi nhánh
    @PutMapping("/{id}")
    public ResponseEntity<Branch> updateBranch(@PathVariable Long id, @RequestBody Branch request) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi nhánh"));

        branch.setName(request.getName());
        branch.setAddress(request.getAddress());
        branch.setPhone(request.getPhone());
        branch.setActive(request.isActive());

        Branch updated = branchRepository.save(branch);
        return ResponseEntity.ok(updated);
    }
}