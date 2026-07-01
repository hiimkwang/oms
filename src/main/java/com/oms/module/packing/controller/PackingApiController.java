package com.oms.module.packing.controller;

import com.oms.module.order.entity.Order;
import com.oms.module.packing.dto.PackingItemRequest;
import com.oms.module.packing.dto.PackingOrderRequest;
import com.oms.module.packing.dto.PackingOrderResponse;
import com.oms.module.packing.dto.PackingRecipientRequest;
import com.oms.module.packing.dto.PackingVideoRequest;
import com.oms.module.packing.service.PackingService;
import com.oms.module.product.entity.ProductVariant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/packing")
@RequiredArgsConstructor
public class PackingApiController {

    private final PackingService packingService;

    /** Kiểm tra vận đơn đã có đơn chưa (để cảnh báo trùng, không tự tạo lại). */
    @GetMapping("/orders/by-tracking/{trackingCode}")
    public ResponseEntity<Map<String, Object>> byTracking(@PathVariable String trackingCode) {
        Order o = packingService.findByTracking(trackingCode);
        Map<String, Object> body = new LinkedHashMap<>();
        if (o == null) {
            body.put("exists", false);
        } else {
            body.put("exists", true);
            body.put("orderCode", o.getOrderCode());
            body.put("status", o.getStatus());
        }
        return ResponseEntity.ok(body);
    }

    /** Tạo đơn nháp khi quét vận đơn (nếu người dùng bật "tự tạo đơn"). */
    @PostMapping("/orders")
    public ResponseEntity<PackingOrderResponse> createDraft(@RequestBody PackingOrderRequest req) {
        Order order = packingService.createDraftFromPacking(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(PackingOrderResponse.from(order));
    }

    /** Thêm 1 sản phẩm quét được vào đơn. */
    @PostMapping("/orders/{orderCode}/items")
    public ResponseEntity<PackingOrderResponse> addItem(@PathVariable String orderCode,
                                                        @RequestBody PackingItemRequest req) {
        Order order = packingService.addItemBySku(orderCode, req);
        return ResponseEntity.ok(PackingOrderResponse.from(order));
    }

    /** Sửa đơn giá / số lượng 1 dòng sản phẩm. */
    @PutMapping("/orders/{orderCode}/items/{itemId}")
    public ResponseEntity<PackingOrderResponse> updateItem(@PathVariable String orderCode,
                                                           @PathVariable Long itemId,
                                                           @RequestBody Map<String, Object> body) {
        java.math.BigDecimal price = body.get("unitPrice") != null ? new java.math.BigDecimal(body.get("unitPrice").toString()) : null;
        Integer qty = body.get("quantity") != null ? Integer.valueOf(body.get("quantity").toString()) : null;
        return ResponseEntity.ok(PackingOrderResponse.from(packingService.updateItem(orderCode, itemId, price, qty)));
    }

    /** Xoá 1 dòng sản phẩm khỏi đơn. */
    @DeleteMapping("/orders/{orderCode}/items/{itemId}")
    public ResponseEntity<PackingOrderResponse> removeItem(@PathVariable String orderCode, @PathVariable Long itemId) {
        return ResponseEntity.ok(PackingOrderResponse.from(packingService.removeItem(orderCode, itemId)));
    }

    /** Cập nhật thông tin người nhận (OCR / sửa tay). */
    @PutMapping("/orders/{orderCode}/recipient")
    public ResponseEntity<PackingOrderResponse> updateRecipient(@PathVariable String orderCode,
                                                                @RequestBody PackingRecipientRequest req) {
        Order order = packingService.updateRecipient(orderCode, req);
        return ResponseEntity.ok(PackingOrderResponse.from(order));
    }

    /** Gắn đường dẫn video đóng gói (trên máy client) vào đơn. */
    @PostMapping("/orders/{orderCode}/video")
    public ResponseEntity<PackingOrderResponse> attachVideo(@PathVariable String orderCode,
                                                            @RequestBody PackingVideoRequest req) {
        Order order = packingService.attachVideoPath(orderCode, req.getVideoPath());
        return ResponseEntity.ok(PackingOrderResponse.from(order));
    }

    /** Tra cứu nhanh thông tin sản phẩm theo SKU quét được (không lộ giá vốn). */
    @GetMapping("/lookup/{sku}")
    public ResponseEntity<Map<String, Object>> lookup(@PathVariable String sku) {
        ProductVariant v = packingService.lookupBySku(sku);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sku", v.getSku());
        body.put("name", v.getProductName()
                + (v.getVariantName() != null && !v.getVariantName().isBlank() ? " - " + v.getVariantName() : ""));
        body.put("price", v.getPrice() != null ? v.getPrice() : BigDecimal.ZERO);
        body.put("stock", v.getStockQuantity() != null ? v.getStockQuantity() : 0);
        return ResponseEntity.ok(body);
    }
}
