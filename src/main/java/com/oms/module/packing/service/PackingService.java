package com.oms.module.packing.service;

import com.oms.constant.CommonConstants.OrderStatusConstant;
import com.oms.constant.CommonConstants.PaymentStatusConstant;
import com.oms.module.customer.entity.Customer;
import com.oms.module.customer.repository.CustomerRepository;
import com.oms.module.order.entity.Order;
import com.oms.module.order.entity.OrderActivity;
import com.oms.module.order.entity.OrderDetail;
import com.oms.module.order.repository.OrderRepository;
import com.oms.module.packing.dto.PackingItemRequest;
import com.oms.module.packing.dto.PackingOrderRequest;
import com.oms.module.packing.dto.PackingRecipientRequest;
import com.oms.module.product.entity.Product;
import com.oms.module.product.entity.ProductVariant;
import com.oms.module.product.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Nghiệp vụ trạm đóng gói: tạo đơn NHÁP tự động + thêm sản phẩm theo SKU quét được.
 * Đơn luôn ở trạng thái DRAFT (không trừ kho) để nhân viên soát lại trước khi xác nhận.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PackingService {

    public static final String WALK_IN_CODE = "KHACHLE";

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductVariantRepository variantRepository;

    /** Lấy (hoặc tạo nếu chưa có) khách "Khách lẻ" mặc định để gắn cho đơn đóng gói. */
    @Transactional
    public Customer getOrCreateWalkInCustomer() {
        return customerRepository.findByCode(WALK_IN_CODE).orElseGet(() -> {
            Customer c = new Customer();
            c.setCode(WALK_IN_CODE);
            c.setFullName("Khách lẻ");
            c.setCustomerGroup("Khách lẻ");
            c.setNote("Khách mặc định cho đơn tạo từ trạm đóng gói");
            return customerRepository.save(c);
        });
    }

    /** Tìm đơn hiện có theo mã vận đơn (để cảnh báo trùng, không tự tạo lại). */
    public Order findByTracking(String trackingCode) {
        String t = emptyToNull(trackingCode);
        if (t == null) return null;
        return orderRepository.findFirstByTrackingCodeOrderByCreatedAtDesc(t).orElse(null);
    }

    /** Tạo đơn nháp mới từ vận đơn quét được. */
    @Transactional
    public Order createDraftFromPacking(PackingOrderRequest req) {
        Customer walkIn = getOrCreateWalkInCustomer();
        String code = "DH" + System.currentTimeMillis() + "-"
                + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        Order order = Order.builder()
                .orderCode(code)
                .customer(walkIn)
                .status(OrderStatusConstant.DRAFT)
                .paymentStatus(PaymentStatusConstant.UNPAID)
                .trackingCode(emptyToNull(req.getTrackingCode()))
                .shippingAddress(composeRecipient(req.getRecipientName(), req.getRecipientPhone(), req.getShippingAddress()))
                .note(emptyToNull(req.getNote()))
                .totalAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .shippingFee(BigDecimal.ZERO)
                .codAmount(BigDecimal.ZERO)
                .amountPaid(BigDecimal.ZERO)
                .details(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();

        addActivity(order, "Tạo từ đóng gói",
                "Đơn nháp được tạo tự động tại trạm đóng gói" +
                        (req.getTrackingCode() != null ? " (vận đơn " + req.getTrackingCode() + ")" : ""));

        return orderRepository.save(order);
    }

    /** Thêm 1 sản phẩm (theo SKU quét được) vào đơn nháp. Trùng SKU + chưa có serial -> cộng dồn số lượng. */
    @Transactional
    public Order addItemBySku(String orderCode, PackingItemRequest req) {
        Order order = getDraftOrder(orderCode);
        String scanned = req.getSku() != null ? req.getSku().trim() : "";
        if (scanned.isEmpty()) {
            throw new RuntimeException("Thiếu mã sản phẩm!");
        }
        int qty = (req.getQuantity() != null && req.getQuantity() > 0) ? req.getQuantity() : 1;
        String serial = emptyToNull(req.getSerialNumber());

        ProductVariant variant = resolveVariant(scanned);
        String sku = variant.getSku(); // luôn lưu SKU chuẩn của biến thể (dù quét bằng barcode)
        Product product = variant.getProduct();
        String varName = variant.getVariantName();
        String productName = (product != null ? product.getName() : "Sản phẩm")
                + (varName != null && !varName.trim().isEmpty() ? " - " + varName : "");
        BigDecimal unitPrice = variant.getPrice() != null ? variant.getPrice() : BigDecimal.ZERO;
        BigDecimal costPrice = variant.getCostPrice() != null ? variant.getCostPrice() : BigDecimal.ZERO;

        // Gộp dòng nếu cùng SKU và (cả hai đều không serial) -> tránh đẻ quá nhiều dòng khi quét lố
        OrderDetail merge = null;
        if (serial == null) {
            for (OrderDetail d : order.getDetails()) {
                if (sku.equals(d.getSku()) && d.getSerialNumber() == null
                        && (d.getIsCustom() == null || !d.getIsCustom())) {
                    merge = d;
                    break;
                }
            }
        }

        if (merge != null) {
            merge.setQuantity(merge.getQuantity() + qty);
            merge.setTotalPrice(merge.getUnitPrice().multiply(BigDecimal.valueOf(merge.getQuantity())));
        } else {
            OrderDetail detail = OrderDetail.builder()
                    .order(order)
                    .product(product)
                    .sku(sku)
                    .productName(productName)
                    .quantity(qty)
                    .unitPrice(unitPrice)
                    .costPrice(costPrice)
                    .totalPrice(unitPrice.multiply(BigDecimal.valueOf(qty)))
                    .isCustom(false)
                    .serialNumber(serial)
                    .warrantyMonths(parseWarrantyMonths(product))
                    .build();
            order.getDetails().add(detail);
        }

        recalcTotal(order);
        return orderRepository.save(order);
    }

    /** Cập nhật đơn giá / số lượng của 1 dòng sản phẩm (nhập tay tại trạm đóng gói). */
    @Transactional
    public Order updateItem(String orderCode, Long itemId, BigDecimal unitPrice, Integer quantity) {
        Order order = getDraftOrder(orderCode);
        OrderDetail d = order.getDetails().stream().filter(x -> itemId.equals(x.getId())).findFirst()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dòng sản phẩm!"));
        if (unitPrice != null) {
            if (unitPrice.signum() < 0) throw new RuntimeException("Đơn giá không được âm!");
            d.setUnitPrice(unitPrice);
        }
        if (quantity != null && quantity > 0) d.setQuantity(quantity);
        d.setTotalPrice(d.getUnitPrice().multiply(BigDecimal.valueOf(d.getQuantity())));
        recalcTotal(order);
        return orderRepository.save(order);
    }

    /** Xoá 1 dòng sản phẩm khỏi đơn (quét nhầm). */
    @Transactional
    public Order removeItem(String orderCode, Long itemId) {
        Order order = getDraftOrder(orderCode);
        order.getDetails().removeIf(x -> itemId.equals(x.getId()));
        recalcTotal(order);
        return orderRepository.save(order);
    }

    /** Suy ra số tháng bảo hành từ chuỗi warrantyPeriod của sản phẩm (VD "12 tháng", "1 năm"). */
    private Integer parseWarrantyMonths(Product p) {
        if (p == null || p.getWarrantyPeriod() == null) return null;
        String w = p.getWarrantyPeriod().toLowerCase();
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)").matcher(w);
        if (!m.find()) return null;
        int n = Integer.parseInt(m.group(1));
        if ((w.contains("năm") || w.contains("nam") || w.contains("year")) && !w.contains("tháng") && !w.contains("thang")) n *= 12;
        return n > 0 ? n : null;
    }

    /** Cập nhật thông tin người nhận (tên/SĐT/địa chỉ) sau khi OCR hoặc sửa tay. */
    @Transactional
    public Order updateRecipient(String orderCode, PackingRecipientRequest req) {
        Order order = getDraftOrder(orderCode);
        order.setShippingAddress(composeRecipient(req.getRecipientName(), req.getRecipientPhone(), req.getShippingAddress()));
        if (req.getNote() != null) order.setNote(emptyToNull(req.getNote()));
        order.getDetails().size(); // nạp details trong transaction để tránh LazyInitializationException khi build response
        return orderRepository.save(order);
    }

    /** Gắn đường dẫn video (trên máy client) vào đơn. */
    @Transactional
    public Order attachVideoPath(String orderCode, String videoPath) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng: " + orderCode));
        order.setPackingVideoPath(emptyToNull(videoPath));
        addActivity(order, "Video đóng gói", describeVideo(videoPath));
        order.getDetails().size(); // nạp details trong transaction để tránh LazyInitializationException khi build response
        Order saved = orderRepository.save(order);
        return saved;
    }

    /** Mô tả thân thiện cho lịch sử thao tác (không dump JSON thô ra giao diện). */
    private String describeVideo(String videoPath) {
        if (videoPath == null || videoPath.isBlank()) return "Đã cập nhật video đóng gói.";
        String s = videoPath.trim();
        if (!s.startsWith("{")) return "Đã lưu video đóng gói.";
        java.util.List<String> parts = new java.util.ArrayList<>();
        if (s.contains("\"pano\":\"")) parts.add("toàn cảnh");
        if (s.contains("\"qr\":\"")) parts.add("cam QR");
        if (s.contains("\"merged\":\"")) parts.add("video ghép");
        return parts.isEmpty() ? "Đã lưu video đóng gói."
                : "Đã lưu video đóng gói (" + String.join(" + ", parts) + ").";
    }

    /** Tra cứu nhanh thông tin biến thể theo SKU HOẶC barcode (an toàn, không lộ giá vốn). */
    public ProductVariant lookupBySku(String code) {
        return resolveVariant(code != null ? code : "");
    }

    /** Tìm biến thể theo SKU trước, không thấy thì theo barcode. */
    private ProductVariant resolveVariant(String code) {
        String c = code != null ? code.trim() : "";
        if (c.isEmpty()) throw new RuntimeException("Thiếu mã sản phẩm!");
        ProductVariant v = variantRepository.findBySku(c).orElse(null);
        if (v == null) {
            java.util.List<ProductVariant> byBarcode = variantRepository.findAllByBarcode(c);
            if (!byBarcode.isEmpty()) v = byBarcode.get(0);
        }
        if (v == null) throw new RuntimeException("Không tìm thấy sản phẩm có mã: " + code);
        return v;
    }

    // ------------------------------------------------------------
    private Order getDraftOrder(String orderCode) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng: " + orderCode));
        if (!OrderStatusConstant.DRAFT.equals(order.getStatus())) {
            throw new RuntimeException("Đơn " + orderCode + " không còn ở trạng thái Nháp, không thể chỉnh sửa từ trạm đóng gói!");
        }
        return order;
    }

    private void recalcTotal(Order order) {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderDetail d : order.getDetails()) {
            if (d.getTotalPrice() != null) total = total.add(d.getTotalPrice());
        }
        order.setTotalAmount(total);
    }

    private String composeRecipient(String name, String phone, String address) {
        StringBuilder sb = new StringBuilder();
        if (name != null && !name.isBlank()) sb.append(name.trim());
        if (phone != null && !phone.isBlank()) sb.append(sb.length() > 0 ? " - " : "").append(phone.trim());
        if (address != null && !address.isBlank()) sb.append(sb.length() > 0 ? " - " : "").append(address.trim());
        return sb.length() > 0 ? sb.toString() : null;
    }

    private String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private void addActivity(Order order, String action, String description) {
        OrderActivity activity = OrderActivity.builder()
                .order(order).action(action).description(description)
                .createdBy(getCurrentUser()).createdAt(LocalDateTime.now()).build();
        if (order.getActivities() == null) order.setActivities(new ArrayList<>());
        order.getActivities().add(activity);
    }

    private String getCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                if (auth.getPrincipal() instanceof com.oms.module.account.entity.User u) {
                    return u.getFullName();
                }
                return auth.getName();
            }
        } catch (Exception ignored) {
        }
        return "Hệ thống";
    }
}
