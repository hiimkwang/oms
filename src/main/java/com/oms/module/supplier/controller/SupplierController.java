package com.oms.module.supplier.controller;
import com.oms.module.receipt.dto.SupplierStatsResponse;
import com.oms.module.receipt.service.ReceiptService;
import com.oms.module.supplier.dto.SupplierRequest;
import com.oms.module.supplier.entity.Supplier;
import com.oms.module.supplier.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/suppliers")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;
    private final ReceiptService receiptService;

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
    @PostMapping("/bulk-delete")
    public ResponseEntity<?> bulkDelete(@RequestBody List<String> codes) {
        try {
            // Viết logic xóa danh sách NCC theo Code vào SupplierService
            supplierService.bulkDeleteByCodes(codes);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/{code}/stats")
    public ResponseEntity<SupplierStatsResponse> getStats(
            @PathVariable String code,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        return ResponseEntity.ok(receiptService.getSupplierStats(code, start, end));
    }

    @PutMapping("/{code}/status")
    public ResponseEntity<?> updateSupplierStatus(
            @PathVariable String code,
            @RequestParam String status) {
        try {
            // Gọi Service để đổi trạng thái
            supplierService.updateStatus(code, status);

            // Trả về JSON thông báo thành công
            return ResponseEntity.ok().body("Cập nhật trạng thái thành công");
        } catch (Exception e) {
            // Lỗi thì ném về 400 Bad Request kèm lý do
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}