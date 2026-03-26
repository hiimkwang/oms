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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerService customerService;
    private final ProductService productService;

    @Transactional
    public Order createOrder(OrderRequest request) {
        // 1. Kiểm tra tồn tại mã đơn
        if (orderRepository.existsByOrderCode(request.getOrderCode())) {
            throw new RuntimeException("Mã đơn hàng đã tồn tại: " + request.getOrderCode());
        }

        // 2. Lấy thông tin khách hàng (Lưu ý: Nếu giao diện gửi ID, hãy dùng findById)
        Customer customer = customerService.getCustomerByCode(request.getCustomerCode());

        // 3. Khởi tạo Order với các trường mới
        Order order = Order.builder()
                .orderCode(request.getOrderCode())
                .salesChannel(request.getSalesChannel())
                .paymentMethod(request.getPaymentMethod())
                .status(request.getStatus())
                .note(request.getNote())
                .customer(customer)
                .orderDetails(new ArrayList<>())
                .build();

        // Xử lý ngày đặt hàng nếu có gửi từ UI
        if (request.getCreatedAt() != null) {
            order.setCreatedAt(LocalDateTime.parse(request.getCreatedAt().substring(0, 10)));
        }

        BigDecimal totalOrderAmount = BigDecimal.ZERO;

        for (OrderDetailRequest detailReq : request.getDetails()) {
            Product product = productService.getProductBySku(detailReq.getSku());

            // Lấy giá: ưu tiên giá nhập từ UI, không có thì lấy giá mặc định của Product
            BigDecimal unitPrice = detailReq.getUnitPrice() != null
                    ? detailReq.getUnitPrice()
                    : product.getPrice(); // Giả định product.getPrice() trả về BigDecimal

            BigDecimal quantity = BigDecimal.valueOf(detailReq.getQuantity());
            BigDecimal discount = detailReq.getDiscount() != null ? detailReq.getDiscount() : BigDecimal.ZERO;

            // Thành tiền = (Đơn giá * Số lượng) - Chiết khấu
            BigDecimal lineTotal = unitPrice.multiply(quantity).subtract(discount);

            OrderDetail orderDetail = OrderDetail.builder()
                    .order(order)
                    .product(product)
                    .quantity(detailReq.getQuantity())
                    .unitPrice(unitPrice)
                    .discount(discount)
                    .totalPrice(lineTotal)
                    .build();

            order.getOrderDetails().add(orderDetail);
            totalOrderAmount = totalOrderAmount.add(lineTotal);
        }

        order.setTotalAmount(totalOrderAmount);
        return orderRepository.save(order);
    }

    public Order getOrderByCode(String orderCode) {
        return orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng: " + orderCode));
    }
}