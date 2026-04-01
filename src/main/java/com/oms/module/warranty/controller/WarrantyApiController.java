package com.oms.module.warranty.controller;

import com.oms.module.order.entity.Order;
import com.oms.module.order.entity.OrderDetail;
import com.oms.module.order.repository.OrderDetailRepository;
import com.oms.module.order.repository.OrderRepository;
import com.oms.module.warranty.entity.WarrantyTicket;
import com.oms.module.warranty.repository.WarrantyTicketRepository;
import com.oms.module.warranty.service.WarrantyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/warranties")
@RequiredArgsConstructor
public class WarrantyApiController {

    private final WarrantyService warrantyService;
    private final OrderDetailRepository orderDetailRepository; // Tiêm Repository mới vào

    @PostMapping
    public ResponseEntity<?> createTicket(@RequestBody WarrantyTicket request) {
        WarrantyTicket saved = warrantyService.createTicket(request);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/bulk")
    public ResponseEntity<?> deleteBulk(@RequestBody List<Long> ids) {
        warrantyService.deleteBulk(ids);
        return ResponseEntity.ok("Xóa thành công");
    }

    // --- API CHECK SERIAL HÀNG REAL ---
    @GetMapping("/check-serial")
    public ResponseEntity<?> checkSerial(@RequestParam String serial) {

        // 1. Quét DB tìm sản phẩm có số Serial này
        Optional<OrderDetail> detailOpt = orderDetailRepository.findBySerialNumber(serial);

        if (detailOpt.isEmpty()) {
            // Không thấy -> Quăng 404 cho Frontend tự biết đường hiện UI nhắc nhở
            return ResponseEntity.notFound().build();
        }

        OrderDetail detail = detailOpt.get();
        Order order = detail.getOrder();

        // 2. Map dữ liệu trả về Frontend
        Map<String, Object> result = new HashMap<>();

        // Tên sản phẩm
        result.put("productName", detail.getProductName());

        // Lấy thông tin khách hàng từ Order
        if (order != null && order.getCustomer() != null) {
            result.put("customerName", order.getCustomer().getFullName()); // Đổi tên hàm nếu cần
            result.put("customerPhone", order.getCustomer().getPhone());   // Đổi tên hàm nếu cần
        } else {
            result.put("customerName", "Khách vãng lai");
            result.put("customerPhone", "");
        }

        // 3. Xử lý logic Bảo hành (Bao lô luôn các trường hợp Null)
        LocalDateTime purchaseDate = (order != null && order.getCreatedAt() != null) ? order.getCreatedAt() : LocalDateTime.now();
        LocalDateTime endDate = detail.getWarrantyEndDate();

        // Nếu lúc bán hàng chưa lưu cứng Ngày hết hạn, thì mình tự tính lại:
        if (endDate == null) {
            // Ưu tiên ngày kích hoạt bảo hành, nếu không có thì lấy ngày mua
            LocalDateTime startDate = (detail.getWarrantyStartDate() != null) ? detail.getWarrantyStartDate() : purchaseDate;
            int months = (detail.getWarrantyMonths() != null) ? detail.getWarrantyMonths() : 0;
            endDate = startDate.plusMonths(months);
        }

        // Kiểm tra xem hiện tại đã qua ngày hết hạn chưa
        boolean isWarrantyValid = LocalDateTime.now().isBefore(endDate);

        result.put("purchaseDate", purchaseDate.toString());
        result.put("warrantyEndDate", endDate.toString());
        result.put("isWarrantyValid", isWarrantyValid); // True: Miễn phí, False: Báo giá sửa chữa

        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTicket(@PathVariable Long id, @RequestBody WarrantyTicket request) {
        WarrantyTicket updated = warrantyService.updateTicket(id, request);
        return ResponseEntity.ok(updated);
    }
}
