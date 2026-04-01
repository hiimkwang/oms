package com.oms.module.order.service;

import com.oms.module.customer.entity.Customer;
import com.oms.module.customer.service.CustomerService;
import com.oms.module.inventory.entity.Inventory;
import com.oms.module.inventory.repository.InventoryRepository;
import com.oms.module.order.dto.OrderDetailRequest;
import com.oms.module.order.dto.OrderRequest;
import com.oms.module.order.entity.Order;
import com.oms.module.order.entity.OrderDetail;
import com.oms.module.order.repository.OrderRepository;
import com.oms.module.product.entity.Product;
import com.oms.module.product.entity.ProductVariant;
import com.oms.module.product.repository.ProductVariantRepository; // BỔ SUNG IMPORT
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
    private final InventoryRepository inventoryRepository;

    // BỔ SUNG: Tiêm repo này vào để lấy đúng Variant ID
    private final ProductVariantRepository variantRepository;

    // LẤY DANH SÁCH ĐƠN HÀNG (Mới nhất lên đầu)
    public List<Order> getAllOrders() {
        return orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    // TẠO ĐƠN HÀNG MỚI
    // TẠO ĐƠN HÀNG MỚI
    @Transactional
    public Order createOrder(OrderRequest request) {
        String generatedCode = "DH" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        Customer customer = customerService.getCustomerByCode(request.getCustomerCode());

        Order order = Order.builder()
                .orderCode(generatedCode)
                .customer(customer)
                .salesChannelCode(request.getSalesChannelCode())
                .branchId(request.getBranchId())
                .status(request.getStatus() != null ? request.getStatus() : "Khởi tạo")
                .note(request.getNote())
                .shippingType(request.getShippingType())
                .shipFromBranchId(request.getShipFromBranchId())
                .shippingPartner(request.getShippingPartner())
                .trackingCode(request.getTrackingCode())
                .shippingAddress(request.getShippingAddress())
                .expectedDeliveryDate(request.getExpectedDeliveryDate())
                .shippingFee(request.getShippingFee() != null ? request.getShippingFee() : BigDecimal.ZERO)
                .codAmount(request.getCodAmount() != null ? request.getCodAmount() : BigDecimal.ZERO)
                .shipWeight(request.getShipWeight())
                .paymentStatus(request.getPaymentStatus())
                .paymentMethod(request.getPaymentMethod())
                .amountPaid(request.getAmountPaid() != null ? request.getAmountPaid() : BigDecimal.ZERO)
                .invoiceTaxCode(request.getInvoiceTaxCode())
                .invoiceCompanyName(request.getInvoiceCompanyName())
                .invoiceCompanyAddress(request.getInvoiceCompanyAddress())
                .discountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO)
                .details(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();

        // BƯỚC 1: Xây dựng danh sách chi tiết đơn hàng (Lúc này order.getDetails() đã có dữ liệu)
        buildOrderDetailsAndCalculateTotal(order, request.getDetails());

        // BƯỚC 2: Lưu đơn hàng xuống DB (kèm theo details nhờ Cascade)
        Order savedOrder = orderRepository.save(order);

        // BƯỚC 3: Áp dụng trừ kho (Lúc này savedOrder đã có đầy đủ Details và BranchId)
        applyInventory(savedOrder, savedOrder.getStatus());

        return savedOrder;
    }

    // CẬP NHẬT ĐƠN HÀNG
    @Transactional
    public Order updateOrder(String orderCode, OrderRequest request) {
        Order order = getOrderByCode(orderCode);
        String oldStatus = order.getStatus();
        String newStatus = request.getStatus();

        if ("Đã hủy".equals(oldStatus)) {
            throw new RuntimeException("Không thể sửa đơn hàng đã bị hủy!");
        }
        if ("Hoàn thành".equals(oldStatus) && !"Đã hủy".equals(newStatus)) {
            throw new RuntimeException("Đơn hàng đã hoàn thành chỉ có thể chuyển sang trạng thái Hủy!");
        }

        // BƯỚC 1: Hoàn trả số lượng cũ về kho TRƯỚC KHI xóa Details
        revertInventory(order, oldStatus);

        Customer customer = customerService.getCustomerByCode(request.getCustomerCode());
        order.setCustomer(customer);
        order.setSalesChannelCode(request.getSalesChannelCode());
        order.setBranchId(request.getBranchId());
        order.setStatus(newStatus);
        order.setNote(request.getNote());

        order.setShippingType(request.getShippingType());
        order.setShipFromBranchId(request.getShipFromBranchId());
        order.setShippingPartner(request.getShippingPartner());
        order.setTrackingCode(request.getTrackingCode());
        order.setShippingAddress(request.getShippingAddress());
        order.setExpectedDeliveryDate(request.getExpectedDeliveryDate());
        order.setShippingFee(request.getShippingFee() != null ? request.getShippingFee() : BigDecimal.ZERO);
        order.setCodAmount(request.getCodAmount() != null ? request.getCodAmount() : BigDecimal.ZERO);
        order.setShipWeight(request.getShipWeight());

        order.setPaymentStatus(request.getPaymentStatus());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setAmountPaid(request.getAmountPaid() != null ? request.getAmountPaid() : BigDecimal.ZERO);

        order.setInvoiceTaxCode(request.getInvoiceTaxCode());
        order.setInvoiceCompanyName(request.getInvoiceCompanyName());
        order.setInvoiceCompanyAddress(request.getInvoiceCompanyAddress());
        order.setDiscountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO);

        // Xóa Details cũ, build lại Details mới theo request sửa
        order.getDetails().clear();
        buildOrderDetailsAndCalculateTotal(order, request.getDetails());

        if (!"Hoàn thành".equals(oldStatus) && "Hoàn thành".equals(newStatus)) {
            for (OrderDetail detail : order.getDetails()) {
                if (detail.getWarrantyMonths() != null && detail.getWarrantyMonths() > 0) {
                    detail.setWarrantyStartDate(LocalDateTime.now());
                    detail.setWarrantyEndDate(LocalDateTime.now().plusMonths(detail.getWarrantyMonths()));
                }
            }
        }

        // BƯỚC 2: ÁP DỤNG LẠI TỒN KHO MỚI
        applyInventory(order, newStatus);

        return orderRepository.save(order);
    }

    // XÓA ĐƠN HÀNG
    @Transactional
    public void deleteOrder(String orderCode) {
        Order order = getOrderByCode(orderCode);
        revertInventory(order, order.getStatus());
        orderRepository.delete(order);
    }

    public Order getOrderByCode(String orderCode) {
        return orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng: " + orderCode));
    }

    private void buildOrderDetailsAndCalculateTotal(Order order, List<OrderDetailRequest> detailRequests) {
        BigDecimal totalItemsAmount = BigDecimal.ZERO;

        for (OrderDetailRequest detailReq : detailRequests) {
            Product product = null;
            String productName = detailReq.getName();

            if (detailReq.getIsCustom() == null || !detailReq.getIsCustom()) {
                try {
                    product = productService.getProductBySku(detailReq.getSku());
                    productName = product.getName();
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
                    .note(detailReq.getNote())
                    .isCustom(detailReq.getIsCustom() != null ? detailReq.getIsCustom() : false)
                    .serialNumber(detailReq.getSerialNumber())
                    .warrantyMonths(detailReq.getWarrantyMonths())
                    .build();

            order.getDetails().add(orderDetail);
            totalItemsAmount = totalItemsAmount.add(lineTotal);
        }

        BigDecimal finalTotal = totalItemsAmount.add(order.getShippingFee()).subtract(order.getDiscountAmount());
        order.setTotalAmount(finalTotal.compareTo(BigDecimal.ZERO) > 0 ? finalTotal : BigDecimal.ZERO);
    }

    public List<Order> findTop10ByCustomer_CodeOrderByCreatedAtDescByCode(String orderCode) {
        return orderRepository.findTop10ByCustomer_CodeOrderByCreatedAtDesc(orderCode);
    }

    // =========================================================
    // CÁC HÀM XỬ LÝ TỒN KHO ĐÃ ĐƯỢC FIX LỖI TÌM THEO VARIANT ID
    // =========================================================

    /**
     * TRỪ KHO (Áp dụng khi Tạo đơn hoặc sau khi Sửa đơn)
     */
    /**
     * TRỪ KHO (ÉP BÁO LỖI NẾU KHÔNG TÌM THẤY DỮ LIỆU)
     */
    /**
     * TRỪ KHO VÀ TỰ ĐỘNG CHỮA LỖI DATA
     */
    private void applyInventory(Order order, String status) {
        if ("Đã hủy".equals(status)) return;
        boolean isCompleted = "Hoàn thành".equals(status);

        Long branchId = order.getShipFromBranchId() != null ? order.getShipFromBranchId() : order.getBranchId();

        if (branchId == null) {
            throw new RuntimeException("Không xác định được chi nhánh xuất hàng!");
        }

        for (OrderDetail detail : order.getDetails()) {
            if (detail.getIsCustom() != null && detail.getIsCustom()) continue;

            ProductVariant variant = variantRepository.findBySku(detail.getSku())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể có mã SKU: " + detail.getSku()));

            // Ép văng lỗi nếu không có hàng tại chi nhánh này (để Frontend in ra chữ đỏ)
            Inventory inv = inventoryRepository.findByVariantIdAndBranchId(variant.getId(), branchId)
                    .orElseThrow(() -> new RuntimeException("Sản phẩm [" + detail.getProductName() + "] chưa từng được nhập vào chi nhánh này!"));

            // TÍNH NĂNG TỰ ĐỘNG CHỮA BỆNH LỖI DATA (SELF-HEALING)
            if (inv.getAvailableStock() > inv.getStock()) {
                inv.setAvailableStock(inv.getStock()); // Ép nó bằng số tồn vật lý
            }

            // Kiểm tra xem có đủ hàng để bán không
            if (inv.getAvailableStock() < detail.getQuantity() && !isCompleted) {
                throw new RuntimeException("Sản phẩm [" + detail.getProductName() + "] chỉ còn " + inv.getAvailableStock() + " chiếc có thể bán tại kho này!");
            }

            // Trừ Có thể bán
            inv.setAvailableStock(Math.max(0, inv.getAvailableStock() - detail.getQuantity()));

            // Nếu Hoàn thành thì trừ Tồn thực tế
            if (isCompleted) {
                inv.setStock(Math.max(0, inv.getStock() - detail.getQuantity()));
            }
            inventoryRepository.save(inv);
        }
    }

    /**
     * NHẢ KHO VÀ TỰ ĐỘNG CHỮA LỖI DATA
     */
    private void revertInventory(Order order, String status) {
        if ("Đã hủy".equals(status)) return;
        boolean isCompleted = "Hoàn thành".equals(status);
        Long branchId = order.getShipFromBranchId() != null ? order.getShipFromBranchId() : order.getBranchId();

        if (branchId == null) return;

        for (OrderDetail detail : order.getDetails()) {
            if (detail.getIsCustom() != null && detail.getIsCustom()) continue;

            ProductVariant variant = variantRepository.findBySku(detail.getSku()).orElse(null);
            if (variant != null) {
                Inventory inv = inventoryRepository.findByVariantIdAndBranchId(variant.getId(), branchId)
                        .orElse(Inventory.builder().variantId(variant.getId()).branchId(branchId).stock(0).availableStock(0).build());

                // Tự chữa bệnh trước khi nhả kho
                if (inv.getAvailableStock() > inv.getStock()) {
                    inv.setAvailableStock(inv.getStock());
                }

                inv.setAvailableStock(inv.getAvailableStock() + detail.getQuantity());

                if (isCompleted) {
                    inv.setStock(inv.getStock() + detail.getQuantity());
                }
                inventoryRepository.save(inv);
            }
        }
    }
}