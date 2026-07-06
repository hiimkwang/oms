package com.oms.module.order.controller;

import com.oms.module.order.dto.OrderRequest;
import com.oms.module.order.entity.Order;
import com.oms.module.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // LẤY DANH SÁCH (PHÂN TRANG + LỌC PHÍA BACKEND)
    // Trả về: content (đơn của trang hiện tại), totalElements, totalPages, page, size,
    // và totalAmountSum = tổng "Khách phải trả" của TOÀN BỘ tập đã lọc (cho dòng tổng cuối bảng).
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) String preset,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime start,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime end) {

        // Chỉ áp lọc ngày khi preset khác null/'all' (hoặc client gửi thẳng start/end)
        java.time.LocalDateTime startTime = null, endTime = null;
        if (preset != null && !preset.isBlank() && !"all".equalsIgnoreCase(preset)) {
            com.oms.utility.DateRangeUtil.DateRange r = com.oms.utility.DateRangeUtil.resolve(preset, start, end);
            startTime = r.start();
            endTime = r.end();
        } else if (start != null || end != null) {
            startTime = start;
            endTime = end;
        }

        var pageable = org.springframework.data.domain.PageRequest.of(
                Math.max(0, page), size <= 0 ? 15 : size,
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));

        org.springframework.data.domain.Page<Order> pageData =
                orderService.searchOrders(keyword, status, channel, startTime, endTime, pageable);
        BigDecimal totalAmountSum = orderService.sumFilteredAmount(keyword, status, channel, startTime, endTime);

        List<Order> content = pageData.getContent();
        boolean admin = isAdmin();
        if (!admin) {
            content.forEach(this::hideCost);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("content", content);
        body.put("totalElements", pageData.getTotalElements());
        body.put("totalPages", pageData.getTotalPages());
        body.put("page", pageData.getNumber());
        body.put("size", pageData.getSize());
        body.put("totalAmountSum", totalAmountSum != null ? totalAmountSum : BigDecimal.ZERO);
        // Chỉ ADMIN mới nhận tổng Lãi/Lỗ (cột này ẩn với STAFF -> không lộ giá vốn)
        if (admin) {
            BigDecimal totalProfitSum = orderService.sumFilteredProfit(keyword, status, channel, startTime, endTime);
            body.put("totalProfitSum", totalProfitSum != null ? totalProfitSum : BigDecimal.ZERO);
        }
        return ResponseEntity.ok(body);
    }

    // Nhân viên (STAFF) không được xem giá vốn -> ẩn khỏi JSON trả về
    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    private void hideCost(Order o) {
        if (o == null || o.getDetails() == null) return;
        o.getDetails().forEach(d -> d.setCostPrice(BigDecimal.ZERO));
    }

    // TẠO MỚI (Và Tạo Nháp)
    @PostMapping
    public ResponseEntity<Order> createOrder(@Valid @RequestBody OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(request));
    }

    // LẤY CHI TIẾT
    @GetMapping("/{orderCode}")
    public ResponseEntity<Order> getOrderByCode(@PathVariable String orderCode) {
        Order order = orderService.getOrderByCode(orderCode);
        if (!isAdmin()) {
            hideCost(order);
        }
        return ResponseEntity.ok(order);
    }

    // SỬA ĐƠN HÀNG
    @PutMapping("/{orderCode}")
    public ResponseEntity<Order> updateOrder(@PathVariable String orderCode, @Valid @RequestBody OrderRequest request) {
        return ResponseEntity.ok(orderService.updateOrder(orderCode, request));
    }

    // ĐỔI TRẠNG THÁI HÀNG LOẠT
    // Body: { "orderCodes": ["DH...", "DH..."], "status": "SHIPPING" }
    // Mỗi đơn xử lý độc lập: đơn lỗi được báo riêng, các đơn còn lại vẫn được cập nhật.
    @PatchMapping("/bulk-status")
    public ResponseEntity<?> updateStatusBulk(@RequestBody Map<String, Object> payload) {
        Object codesObj = payload.get("orderCodes");
        String status = payload.get("status") != null ? payload.get("status").toString() : null;

        if (!(codesObj instanceof List<?> rawCodes) || rawCodes.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Vui lòng chọn ít nhất 1 đơn hàng!"));
        }
        if (status == null || status.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Vui lòng chọn trạng thái cần chuyển!"));
        }

        int success = 0;
        List<Map<String, String>> failures = new ArrayList<>();

        for (Object codeObj : rawCodes) {
            String code = codeObj != null ? codeObj.toString() : null;
            if (code == null || code.isBlank()) continue;
            try {
                orderService.changeStatus(code, status);
                success++;
            } catch (Exception e) {
                failures.add(Map.of("orderCode", code, "error", e.getMessage() != null ? e.getMessage() : "Lỗi không xác định"));
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("failed", failures.size());
        result.put("failures", failures);
        result.put("total", rawCodes.size());
        return ResponseEntity.ok(result);
    }

    // XÓA ĐƠN HÀNG (chỉ ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{orderCode}")
    public ResponseEntity<?> deleteOrder(@PathVariable String orderCode) {
        orderService.deleteOrder(orderCode);
        return ResponseEntity.ok(Map.of("message", "Đã xóa đơn hàng " + orderCode));
    }
}