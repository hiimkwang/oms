package com.oms.module.supplier.controller;
import com.oms.module.supplier.dto.SupplierRequest;
import com.oms.module.supplier.entity.Supplier;
import com.oms.module.supplier.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/suppliers")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    // Lấy danh sách NCC (Có tìm kiếm)
    @GetMapping
    public ResponseEntity<List<Supplier>> getSuppliers(@RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(supplierService.getSuppliers(keyword));
    }

    // Lấy chi tiết 1 NCC theo Mã
    @GetMapping("/{supplierCode}")
    public ResponseEntity<Supplier> getSupplierByCode(@PathVariable String supplierCode) {
        return ResponseEntity.ok(supplierService.getSupplierByCode(supplierCode));
    }

    // Thêm mới NCC
    @PostMapping
    public ResponseEntity<Supplier> createSupplier(@Valid @RequestBody SupplierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.createSupplier(request));
    }

    // Cập nhật thông tin NCC
    @PutMapping("/{supplierCode}")
    public ResponseEntity<Supplier> updateSupplier(
            @PathVariable String supplierCode,
            @Valid @RequestBody SupplierRequest request) {
        return ResponseEntity.ok(supplierService.updateSupplier(supplierCode, request));
    }
}