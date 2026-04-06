package com.oms.module.order.service;

import com.oms.module.customer.entity.Customer;
import com.oms.module.customer.service.CustomerService;
import com.oms.module.inventory.entity.Inventory;
import com.oms.module.inventory.repository.InventoryRepository;
import com.oms.module.notification.entity.Notification;
import com.oms.module.notification.service.NotificationService;
import com.oms.module.order.dto.OrderDetailRequest;
import com.oms.module.order.dto.OrderRequest;
import com.oms.module.order.entity.Order;
import com.oms.module.order.entity.OrderActivity;
import com.oms.module.order.entity.OrderDetail;
import com.oms.module.order.repository.OrderRepository;
import com.oms.module.product.entity.Product;
import com.oms.module.product.entity.ProductVariant;
import com.oms.module.product.repository.ProductVariantRepository;
import com.oms.module.product.service.ProductService;
import com.oms.module.setting.entity.MasterData;
import com.oms.module.setting.service.MasterDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.oms.constant.CommonConstants.OrderStatusConstant.*;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerService customerService;
    private final ProductService productService;
    private final InventoryRepository inventoryRepository;
    private final NotificationService notificationService;
    private final ProductVariantRepository variantRepository;
    private final MasterDataService masterDataService;

    // LẤY DANH SÁCH ĐƠN HÀNG
    public List<Order> getAllOrders() {
        return orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    // TẠO ĐƠN HÀNG MỚI
    @Transactional
    public Order createOrder(OrderRequest request) {
        String generatedCode = "DH" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        Customer customer = customerService.getCustomerByCode(request.getCustomerCode());

        String requestedStatus = request.getStatus() != null ? request.getStatus() : DRAFT;

        Order order = Order.builder()
                .orderCode(generatedCode)
                .customer(customer)
                .salesChannelCode(request.getSalesChannelCode())
                .branchId(request.getBranchId())
                .status(requestedStatus)
                .note(request.getNote())
                .shippingType(request.getShippingType())
                .shipFromBranchId(request.getShipFromBranchId())
                .shippingPartner(request.getShippingPartner())
                .trackingCode(request.getTrackingCode())
                .referenceCode(request.getReferenceCode())
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
                .createdAt(request.getCreatedAt() != null ? request.getCreatedAt() : LocalDateTime.now())
                .build();

        buildOrderDetailsAndCalculateTotal(order, request.getDetails());
        Order savedOrder = orderRepository.save(order);

        // 1. NẾU LÀ ĐƠN NHÁP
        if (DRAFT.equals(requestedStatus)) {
            addActivityLog(savedOrder, "Lưu nháp", "Đơn hàng được lưu nháp");
            return savedOrder;
        }

        // 2. NẾU LÀ TẠO ĐƠN (CREATED) -> TỰ ĐỘNG CHUYỂN SANG ĐÃ XÁC NHẬN (CONFIRMED)
        if (CREATED.equals(requestedStatus)) {
            addActivityLog(savedOrder, "Tạo mới đơn hàng", "Khởi tạo đơn hàng thành công");
            savedOrder.setStatus(CONFIRMED);
            addActivityLog(savedOrder, "Xác nhận đơn", "Hệ thống tự động chuyển sang Đã xác nhận");
            savedOrder = orderRepository.save(savedOrder);
        } else {
            // Trường hợp tạo thẳng ở trạng thái khác (VD: SHIPPING)
            addActivityLog(savedOrder, "Tạo mới đơn hàng", "Khởi tạo đơn hàng ở trạng thái: " + translateStatus(requestedStatus));
        }

        // 3. TRỪ KHO VÀ BÁO CHUÔNG
        applyInventory(savedOrder, savedOrder.getStatus());
        notificationService.create(
                "Đơn hàng mới",
                "Đơn hàng " + savedOrder.getOrderCode() + " vừa được tạo thành công.",
                Notification.NotificationType.ORDER,
                "/ui/orders/detail/" + savedOrder.getOrderCode()
        );

        return savedOrder;
    }

    // CẬP NHẬT ĐƠN HÀNG
    @Transactional
    public Order updateOrder(String orderCode, OrderRequest request) {
        Order order = getOrderByCode(orderCode);
        String oldStatus = order.getStatus();
        String newStatus = request.getStatus();

        // Tự động nhảy sang Đã xác nhận nếu đang sửa từ Nháp -> Tạo đơn
        if (CREATED.equals(newStatus)) {
            newStatus = CONFIRMED;
        }

        if (CANCELLED.equals(oldStatus)) {
            throw new RuntimeException("Không thể sửa đơn hàng đã bị hủy!");
        }
        if (COMPLETED.equals(oldStatus) && !CANCELLED.equals(newStatus)) {
            throw new RuntimeException("Đơn hàng đã hoàn thành chỉ có thể chuyển sang trạng thái Hủy!");
        }

        revertInventory(order, oldStatus);

        Customer customer = customerService.getCustomerByCode(request.getCustomerCode());
        order.setCustomer(customer);
        order.setSalesChannelCode(request.getSalesChannelCode());
        order.setBranchId(request.getBranchId());
        order.setStatus(newStatus);
        order.setNote(request.getNote());
        order.setReferenceCode(request.getReferenceCode());

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

        if (!COMPLETED.equals(oldStatus) && COMPLETED.equals(newStatus)) {
            for (OrderDetail detail : order.getDetails()) {
                if (detail.getWarrantyMonths() != null && detail.getWarrantyMonths() > 0) {
                    detail.setWarrantyStartDate(LocalDateTime.now());
                    detail.setWarrantyEndDate(LocalDateTime.now().plusMonths(detail.getWarrantyMonths()));
                }
            }
        }

        // GHI LOG VÀ RUNG CHUÔNG NẾU ĐỔI TRẠNG THÁI
        if (!oldStatus.equals(newStatus)) {
            addActivityLog(order, "Cập nhật trạng thái", "Chuyển trạng thái từ [" + translateStatus(oldStatus) + "] sang [" + translateStatus(newStatus) + "]");
            if (CANCELLED.equals(newStatus) || COMPLETED.equals(newStatus)) {
                String translatedStatus = translateStatus(newStatus);
                notificationService.create(
                        "Cập nhật đơn hàng " + order.getOrderCode(),
                        "Đơn hàng đã chuyển sang trạng thái: " + translatedStatus,
                        Notification.NotificationType.ORDER,
                        "/ui/orders/detail/" + order.getOrderCode()
                );
            }
        } else if (order.getAmountPaid().compareTo(request.getAmountPaid()) != 0) {
            addActivityLog(order, "Thanh toán", "Cập nhật số tiền đã thanh toán: " + request.getAmountPaid() + "đ");
        } else {
            addActivityLog(order, "Cập nhật thông tin", "Thay đổi thông tin chi tiết đơn hàng");
        }

        // ÁP DỤNG LẠI TỒN KHO MỚI
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
        return orderRepository.findByOrderCode(orderCode).orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng: " + orderCode));
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

    /**
     * TRỪ KHO (Áp dụng khi Tạo đơn hoặc sau khi Sửa đơn)
     */
    private void applyInventory(Order order, String status) {
        if (CANCELLED.equals(status) || DRAFT.equals(status)) return;
        boolean isCompleted = COMPLETED.equals(status);

        Long branchId = order.getShipFromBranchId() != null ? order.getShipFromBranchId() : order.getBranchId();

        if (branchId == null) {
            throw new RuntimeException("Không xác định được chi nhánh xuất hàng!");
        }

        for (OrderDetail detail : order.getDetails()) {
            if (detail.getIsCustom() != null && detail.getIsCustom()) continue;

            ProductVariant variant = variantRepository.findBySku(detail.getSku()).orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể có mã SKU: " + detail.getSku()));

            // Ép văng lỗi nếu không có hàng tại chi nhánh này
            Inventory inv = inventoryRepository.findByVariantIdAndBranchId(variant.getId(), branchId).orElseThrow(() -> new RuntimeException("Sản phẩm [" + detail.getProductName() + "] chưa từng được nhập vào chi nhánh này!"));

            if (inv.getAvailableStock() > inv.getStock()) {
                inv.setAvailableStock(inv.getStock());
            }

            // Kiểm tra xem có đủ hàng để bán không
            if (inv.getAvailableStock() < detail.getQuantity() && !isCompleted) {
                throw new RuntimeException("Sản phẩm [" + detail.getProductName() + "] chỉ còn " + inv.getAvailableStock() + " chiếc có thể bán tại kho này!");
            }

            // Trừ Có thể bán (Ở chi nhánh)
            inv.setAvailableStock(Math.max(0, inv.getAvailableStock() - detail.getQuantity()));

            // Nếu Hoàn thành thì trừ Tồn thực tế
            if (isCompleted) {
                // 1. Trừ tồn kho vật lý ở chi nhánh
                inv.setStock(Math.max(0, inv.getStock() - detail.getQuantity()));

                // 2. Trừ tổng tồn kho toàn hệ thống của Biến thể (ProductVariant)
                int currentVariantStock = variant.getStockQuantity() != null ? variant.getStockQuantity() : 0;
                variant.setStockQuantity(Math.max(0, currentVariantStock - detail.getQuantity()));
                variantRepository.save(variant);

                // 3. Gọi hàm đồng bộ tổng tồn kho lên Sản phẩm cha (Product)
                if (variant.getProduct() != null) {
                    productService.syncProductTotalStock(variant.getProduct().getId());
                }
            }

            inventoryRepository.save(inv);
        }
    }

    /**
     * NHẢ KHO
     */
    private void revertInventory(Order order, String status) {
        if (CANCELLED.equals(status) || DRAFT.equals(status)) return;
        boolean isCompleted = COMPLETED.equals(status);
        Long branchId = order.getShipFromBranchId() != null ? order.getShipFromBranchId() : order.getBranchId();

        if (branchId == null) return;

        for (OrderDetail detail : order.getDetails()) {
            if (detail.getIsCustom() != null && detail.getIsCustom()) continue;

            ProductVariant variant = variantRepository.findBySku(detail.getSku()).orElse(null);
            if (variant != null) {
                Inventory inv = inventoryRepository.findByVariantIdAndBranchId(variant.getId(), branchId).orElse(Inventory.builder().variantId(variant.getId()).branchId(branchId).stock(0).availableStock(0).build());

                if (inv.getAvailableStock() > inv.getStock()) {
                    inv.setAvailableStock(inv.getStock());
                }

                // Nhả Có thể bán
                inv.setAvailableStock(inv.getAvailableStock() + detail.getQuantity());

                // Nếu đơn đã Hoàn thành mà bị Hủy/Sửa, nhả Tồn thực tế
                if (isCompleted) {
                    // 1. Nhả tồn kho ở chi nhánh
                    inv.setStock(inv.getStock() + detail.getQuantity());

                    // 2. Nhả tổng tồn kho toàn hệ thống của Biến thể (ProductVariant)
                    int currentVariantStock = variant.getStockQuantity() != null ? variant.getStockQuantity() : 0;
                    variant.setStockQuantity(currentVariantStock + detail.getQuantity());
                    variantRepository.save(variant);

                    // 3. Gọi hàm đồng bộ tổng tồn kho lên Sản phẩm cha (Product)
                    if (variant.getProduct() != null) {
                        productService.syncProductTotalStock(variant.getProduct().getId());
                    }
                }
                inventoryRepository.save(inv);
            }
        }
    }

    private String getCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
                if (auth.getPrincipal() instanceof com.oms.module.account.entity.User) {
                    return ((com.oms.module.account.entity.User) auth.getPrincipal()).getFullName();
                }
                return auth.getName();
            }
        } catch (Exception e) {
        }
        return "Hệ thống";
    }

    private void addActivityLog(Order order, String action, String description) {
        OrderActivity activity = OrderActivity.builder()
                .order(order)
                .action(action)
                .description(description)
                .createdBy(getCurrentUser())
                .createdAt(LocalDateTime.now())
                .build();
        if (order.getActivities() == null) {
            order.setActivities(new ArrayList<>());
        }
        order.getActivities().add(activity);
    }

    private String translateStatus(String statusValue) {
        if (statusValue == null) return "---";
        List<MasterData> statuses = masterDataService.getMasterDataByType("ORDER_STATUS");
        for (MasterData data : statuses) {
            if (data.getDataValue().equals(statusValue)) {
                return data.getDataLabel() != null ? data.getDataLabel() : data.getDataValue();
            }
        }
        return statusValue;
    }
}