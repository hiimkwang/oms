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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    // TẠO ĐƠN HÀNG MỚI (Bao gồm cả Đơn Nháp)
    @Transactional
    public Order createOrder(OrderRequest request) {
        // Tự động sinh mã đơn hàng: DH + Timestamp + 4 ký tự ngẫu nhiên
        String generatedCode = "DH" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        Customer customer = customerService.getCustomerByCode(request.getCustomerCode());

        Order order = Order.builder()
                .orderCode(generatedCode)
                .customer(customer)
                .salesChannelCode(request.getSalesChannelCode())
                .branchId(request.getBranchId())
                .status(request.getStatus() != null ? request.getStatus() : "Khởi tạo")
                .note(request.getNote())

                // --- Vận chuyển ---
                .shippingType(request.getShippingType())
                .shipFromBranchId(request.getShipFromBranchId())
                .shippingPartner(request.getShippingPartner())
                .trackingCode(request.getTrackingCode())
                .shippingAddress(request.getShippingAddress())
                .expectedDeliveryDate(request.getExpectedDeliveryDate())
                .shippingFee(request.getShippingFee() != null ? request.getShippingFee() : BigDecimal.ZERO)
                .codAmount(request.getCodAmount() != null ? request.getCodAmount() : BigDecimal.ZERO)
                .shipWeight(request.getShipWeight())

                // --- Thanh toán ---
                .paymentStatus(request.getPaymentStatus())
                .paymentMethod(request.getPaymentMethod())
                .amountPaid(request.getAmountPaid() != null ? request.getAmountPaid() : BigDecimal.ZERO)

                // --- Hóa đơn VAT ---
                .invoiceTaxCode(request.getInvoiceTaxCode())
                .invoiceCompanyName(request.getInvoiceCompanyName())
                .invoiceCompanyAddress(request.getInvoiceCompanyAddress())

                // --- Tiền nong ---
                .discountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO)
                .details(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();

        // Xử lý chi tiết sản phẩm và tính lại tổng tiền
        buildOrderDetailsAndCalculateTotal(order, request.getDetails());

        return orderRepository.save(order);
    }

    // CẬP NHẬT ĐƠN HÀNG
    @Transactional
    public Order updateOrder(String orderCode, OrderRequest request) {
        Order order = getOrderByCode(orderCode);

        // Không cho sửa đơn đã hoàn thành/hủy
        if ("Hoàn thành".equals(order.getStatus()) || "Đã hủy".equals(order.getStatus())) {
            throw new RuntimeException("Không thể sửa đơn hàng đã chốt trạng thái cuối!");
        }

        Customer customer = customerService.getCustomerByCode(request.getCustomerCode());

        order.setCustomer(customer);
        order.setSalesChannelCode(request.getSalesChannelCode());
        order.setBranchId(request.getBranchId());
        order.setStatus(request.getStatus());
        order.setNote(request.getNote());

        // --- Cập nhật Vận chuyển ---
        order.setShippingType(request.getShippingType());
        order.setShipFromBranchId(request.getShipFromBranchId());
        order.setShippingPartner(request.getShippingPartner());
        order.setTrackingCode(request.getTrackingCode());
        order.setShippingAddress(request.getShippingAddress());
        order.setExpectedDeliveryDate(request.getExpectedDeliveryDate());
        order.setShippingFee(request.getShippingFee() != null ? request.getShippingFee() : BigDecimal.ZERO);
        order.setCodAmount(request.getCodAmount() != null ? request.getCodAmount() : BigDecimal.ZERO);
        order.setShipWeight(request.getShipWeight());

        // --- Cập nhật Thanh toán ---
        order.setPaymentStatus(request.getPaymentStatus());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setAmountPaid(request.getAmountPaid() != null ? request.getAmountPaid() : BigDecimal.ZERO);

        // --- Cập nhật Hóa đơn VAT ---
        order.setInvoiceTaxCode(request.getInvoiceTaxCode());
        order.setInvoiceCompanyName(request.getInvoiceCompanyName());
        order.setInvoiceCompanyAddress(request.getInvoiceCompanyAddress());

        // --- Cập nhật Tiền nong ---
        order.setDiscountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO);

        // Xóa các line sản phẩm cũ và đắp line mới vào
        order.getDetails().clear();
        buildOrderDetailsAndCalculateTotal(order, request.getDetails());
        if ("Hoàn thành".equals(request.getStatus()) && !"Hoàn thành".equals(order.getStatus())) {
            for (OrderDetail detail : order.getDetails()) {
                if (detail.getWarrantyMonths() != null && detail.getWarrantyMonths() > 0) {
                    detail.setWarrantyStartDate(LocalDateTime.now());
                    detail.setWarrantyEndDate(LocalDateTime.now().plusMonths(detail.getWarrantyMonths()));
                }
            }
        }
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

    // HÀM PHỤ TRỢ: Build chi tiết đơn hàng và tính tổng tiền
    private void buildOrderDetailsAndCalculateTotal(Order order, List<OrderDetailRequest> detailRequests) {
        BigDecimal totalItemsAmount = BigDecimal.ZERO;

        for (OrderDetailRequest detailReq : detailRequests) {
            Product product = null;
            String productName = detailReq.getName(); // Lấy tên sản phẩm gửi từ frontend

            // Nếu KHÔNG phải hàng Custom (Tùy chỉnh) thì mới đi check DB
            if (detailReq.getIsCustom() == null || !detailReq.getIsCustom()) {
                try {
                    product = productService.getProductBySku(detailReq.getSku());
                    productName = product.getName(); // Lấy tên chuẩn từ DB
                } catch (Exception e) {
                    System.out.println("Cảnh báo: Không tìm thấy sản phẩm có SKU " + detailReq.getSku());
                }
            }

            BigDecimal unitPrice = detailReq.getUnitPrice() != null ? detailReq.getUnitPrice() : BigDecimal.ZERO;
            BigDecimal quantity = BigDecimal.valueOf(detailReq.getQuantity());
            BigDecimal lineTotal = unitPrice.multiply(quantity);

            OrderDetail orderDetail = OrderDetail.builder()
                    .order(order)
                    .product(product)
                    .sku(detailReq.getSku())
                    .productName(productName)
                    .quantity(detailReq.getQuantity())
                    .unitPrice(unitPrice)
                    .totalPrice(lineTotal)
                    .note(detailReq.getNote()) // Lưu ghi chú line (đổi size, bọc quà...)
                    .isCustom(detailReq.getIsCustom() != null ? detailReq.getIsCustom() : false)
                    .serialNumber(detailReq.getSerialNumber())
                    .warrantyMonths(detailReq.getWarrantyMonths())
                    .build();

            order.getDetails().add(orderDetail);
            totalItemsAmount = totalItemsAmount.add(lineTotal);
        }

        // Tính Thành Tiền = (Tổng tiền hàng + Phí Ship) - Giảm Giá
        BigDecimal finalTotal = totalItemsAmount.add(order.getShippingFee()).subtract(order.getDiscountAmount());

        // Đảm bảo tổng tiền không bao giờ bị âm
        order.setTotalAmount(finalTotal.compareTo(BigDecimal.ZERO) > 0 ? finalTotal : BigDecimal.ZERO);
    }
}