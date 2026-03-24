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
        if (orderRepository.existsByOrderCode(request.getOrderCode())) {
            throw new RuntimeException("Mã phiếu/Đơn hàng đã tồn tại: " + request.getOrderCode());
        }

        // Lấy thông tin khách hàng
        Customer customer = customerService.getCustomerByCode(request.getCustomerCode());

        // Khởi tạo Order
        Order order = Order.builder()
                .orderCode(request.getOrderCode())
                .salesChannel(request.getSalesChannel())
                .paymentMethod(request.getPaymentMethod())
                .status(request.getStatus())
                .note(request.getNote())
                .customer(customer)
                .orderDetails(new ArrayList<>())
                .build();

        double totalOrderAmount = 0.0;

        // Xử lý từng chi tiết đơn hàng
        for (OrderDetailRequest detailReq : request.getDetails()) {
            Product product = productService.getProductBySku(detailReq.getSku());

            // Nếu trên giao diện có nhập đơn giá khác thì lấy giá đó, nếu không thì lấy Giá bán đề xuất mặc định của sản phẩm
            double unitPrice = detailReq.getUnitPrice() != null ? detailReq.getUnitPrice() : Double.valueOf(String.valueOf(product.getRetailPrice()));
            double discount = detailReq.getDiscount() != null ? detailReq.getDiscount() : 0.0;
            double totalPrice = (unitPrice * detailReq.getQuantity()) - discount;

            OrderDetail orderDetail = OrderDetail.builder()
                    .order(order)
                    .product(product)
                    .quantity(detailReq.getQuantity())
                    .unitPrice(unitPrice)
                    .discount(discount)
                    .totalPrice(totalPrice)
                    .build();

            order.getOrderDetails().add(orderDetail);
            totalOrderAmount += totalPrice;
        }

        order.setTotalAmount(totalOrderAmount);

        // Lưu Order (CascadeType.ALL sẽ tự động lưu luôn OrderDetail)
        return orderRepository.save(order);
    }

    public Order getOrderByCode(String orderCode) {
        return orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng: " + orderCode));
    }
}