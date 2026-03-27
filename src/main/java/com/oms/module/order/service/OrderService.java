package com.oms.module.order.service;

import com.oms.module.customer.entity.Customer;
import com.oms.module.customer.service.CustomerService;
import com.oms.module.order.dto.OrderDetailRequest;
import com.oms.module.order.dto.OrderRequest;
import com.oms.module.order.entity.Order;
import com.oms.module.order.entity.OrderDetail;
import com.oms.module.order.repository.OrderRepository;
import com.oms.module.product.entity.Product;
import com.oms.module.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerService customerService;
    private final ProductService productService;

    // LẤY DANH SÁCH ĐƠN HÀNG (Mới nhất lên đầu)
    public List<Order> getAllOrders() {
        return orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    // TẠO ĐƠN HÀNG (Bao gồm cả Đơn Nháp nếu status = "Nháp")
    @Transactional
    public Order createOrder(OrderRequest request) {
        if (orderRepository.existsByOrderCode(request.getOrderCode())) {
            throw new RuntimeException("Mã đơn hàng đã tồn tại: " + request.getOrderCode());
        }

        Customer customer = customerService.getCustomerByCode(request.getCustomerCode());

        Order order = Order.builder()
                .orderCode(request.getOrderCode())
                .salesChannel(request.getSalesChannel())
                .paymentMethod(request.getPaymentMethod())
                .status(request.getStatus() != null ? request.getStatus() : "Khởi tạo")
                .note(request.getNote())
                .customer(customer)
                .shippingFee(BigDecimal.valueOf(request.getShippingFee() != null ? request.getShippingFee() : 0))
                .discount(BigDecimal.valueOf(request.getDiscount() != null ? request.getDiscount() : 0))
                .orderDetails(new ArrayList<>()) // BỔ SUNG DÒNG NÀY ĐỂ FIX LỖI NPE
                .build();

        // Xử lý an toàn ngày đặt hàng từ giao diện (Thường có dạng 2026-03-27T09:18)
        if (request.getCreatedAt() != null && !request.getCreatedAt().isBlank()) {
            try {
                order.setCreatedAt(LocalDateTime.parse(request.getCreatedAt()));
            } catch (Exception e) {
                order.setCreatedAt(LocalDateTime.now());
            }
        }

        buildOrderDetailsAndCalculateTotal(order, request.getDetails());

        return orderRepository.save(order);
    }

    // CẬP NHẬT ĐƠN HÀNG
    @Transactional
    public Order updateOrder(String orderCode, OrderRequest request) {
        Order order = getOrderByCode(orderCode);

        // Không cho sửa đơn đã hoàn thành/hủy
        if (order.getStatus().equals("Hoàn thành") || order.getStatus().equals("Đã hủy")) {
            throw new RuntimeException("Không thể sửa đơn hàng đã chốt trạng thái cuối!");
        }

        Customer customer = customerService.getCustomerByCode(request.getCustomerCode());

        order.setCustomer(customer);
        order.setSalesChannel(request.getSalesChannel());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setStatus(request.getStatus());
        order.setNote(request.getNote());
        order.setShippingFee(BigDecimal.valueOf(request.getShippingFee() != null ? request.getShippingFee() : 0));
        order.setDiscount(BigDecimal.valueOf(request.getDiscount() != null ? request.getDiscount() : 0));

        // Xóa các line cũ và đắp line mới vào
        order.getOrderDetails().clear();
        buildOrderDetailsAndCalculateTotal(order, request.getDetails());

        return orderRepository.save(order);
    }

    // XÓA ĐƠN HÀNG
    @Transactional
    public void deleteOrder(String orderCode) {
        Order order = getOrderByCode(orderCode);
        orderRepository.delete(order);
    }

    public Order getOrderByCode(String orderCode) {
        return orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng: " + orderCode));
    }

    // Hàm phụ trợ: Build chi tiết đơn hàng và tính tổng tiền
    private void buildOrderDetailsAndCalculateTotal(Order order, List<OrderDetailRequest> detailRequests) {
        BigDecimal totalItemsAmount = BigDecimal.ZERO;

        for (OrderDetailRequest detailReq : detailRequests) {
            Product product = null;
            String productName = "Sản phẩm tùy chỉnh";

            // NẾU KHÔNG PHẢI HÀNG CUSTOM THÌ MỚI ĐI TÌM TRONG DB
            if (detailReq.getSku() != null && !detailReq.getSku().startsWith("CUSTOM_")) {
                try {
                    // Chú ý: Cần chắc chắn hàm này tìm đúng Variant nhé
                    product = productService.getProductBySku(detailReq.getSku());
                    productName = product.getName();
                } catch (Exception e) {
                    throw new RuntimeException("Không tìm thấy sản phẩm hoặc biến thể với SKU: " + detailReq.getSku());
                }
            }

            BigDecimal unitPrice = detailReq.getUnitPrice() != null
                    ? detailReq.getUnitPrice()
                    : (product != null ? product.getPrice() : BigDecimal.ZERO);

            BigDecimal quantity = BigDecimal.valueOf(detailReq.getQuantity());
            BigDecimal itemDiscount = detailReq.getDiscount() != null ? detailReq.getDiscount() : BigDecimal.ZERO;

            BigDecimal lineTotal = unitPrice.multiply(quantity).subtract(itemDiscount);

            OrderDetail orderDetail = OrderDetail.builder()
                    .order(order)
                    .product(product) // Có thể null nếu là hàng custom
                    .sku(detailReq.getSku())
                    .productName(productName)
                    .quantity(detailReq.getQuantity())
                    .unitPrice(unitPrice)
                    .discount(itemDiscount)
                    .totalPrice(lineTotal)
                    .build();

            order.getOrderDetails().add(orderDetail);
            totalItemsAmount = totalItemsAmount.add(lineTotal);
        }

        // Tính Thành Tiền = (Tiền Hàng + Phí Ship) - Giảm Giá
        BigDecimal finalTotal = totalItemsAmount.add(order.getShippingFee()).subtract(order.getDiscount());
        order.setTotalAmount(finalTotal.compareTo(BigDecimal.ZERO) > 0 ? finalTotal : BigDecimal.ZERO);
    }
}