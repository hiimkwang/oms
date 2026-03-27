package com.oms.module.order.controller;

import com.oms.module.order.dto.OrderRequest;
import com.oms.module.order.entity.Order;
import com.oms.module.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // LẤY DANH SÁCH
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    // TẠO MỚI (Và Tạo Nháp)
    @PostMapping
    public ResponseEntity<Order> createOrder(@Valid @RequestBody OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(request));
    }

    // LẤY CHI TIẾT
    @GetMapping("/{orderCode}")
    public ResponseEntity<Order> getOrderByCode(@PathVariable String orderCode) {
        return ResponseEntity.ok(orderService.getOrderByCode(orderCode));
    }

    // SỬA ĐƠN HÀNG
    @PutMapping("/{orderCode}")
    public ResponseEntity<Order> updateOrder(@PathVariable String orderCode, @Valid @RequestBody OrderRequest request) {
        return ResponseEntity.ok(orderService.updateOrder(orderCode, request));
    }

    // XÓA ĐƠN HÀNG
    @DeleteMapping("/{orderCode}")
    public ResponseEntity<?> deleteOrder(@PathVariable String orderCode) {
        orderService.deleteOrder(orderCode);
        return ResponseEntity.ok(Map.of("message", "Đã xóa đơn hàng " + orderCode));
    }
}