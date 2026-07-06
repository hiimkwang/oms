package com.oms.module.order.service;

import com.oms.config.exception.BusinessException;
import com.oms.module.cashbook.dto.CashTransactionRequest;
import com.oms.module.cashbook.entity.CashTransaction;
import com.oms.module.cashbook.service.CashbookService;
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
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerService customerService;
    private final ProductService productService;
    private final InventoryRepository inventoryRepository;
    private final NotificationService notificationService;
    private final ProductVariantRepository variantRepository;
    private final MasterDataService masterDataService;
    private final CashbookService cashbookService;

    // LẤY DANH SÁCH ĐƠN HÀNG
    // readOnly: chặn Hibernate auto-flush -> khi Controller ẩn giá vốn (set costPrice=0 cho STAFF)
    // KHÔNG bị ghi nhầm giá trị 0 xuống DB.
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    // DANH SÁCH ĐƠN CÓ PHÂN TRANG + LỌC PHÍA BACKEND
    // Chuẩn hoá tham số: chuỗi rỗng/"ALL" -> null (bỏ lọc); keyword -> '%kw%' viết thường.
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<Order> searchOrders(
            String keyword, String status, String channel,
            LocalDateTime start, LocalDateTime end,
            org.springframework.data.domain.Pageable pageable) {
        String kw = (keyword == null || keyword.isBlank()) ? null
                : "%" + keyword.trim().toLowerCase() + "%";
        String st = (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) ? null : status;
        String ch = (channel == null || channel.isBlank() || "ALL".equalsIgnoreCase(channel)) ? null : channel;
        return orderRepository.searchOrders(kw, st, ch, start, end, pageable);
    }

    @Transactional(readOnly = true)
    public BigDecimal sumFilteredAmount(String keyword, String status, String channel,
                                        LocalDateTime start, LocalDateTime end) {
        String kw = (keyword == null || keyword.isBlank()) ? null
                : "%" + keyword.trim().toLowerCase() + "%";
        String st = (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) ? null : status;
        String ch = (channel == null || channel.isBlank() || "ALL".equalsIgnoreCase(channel)) ? null : channel;
        return orderRepository.sumFilteredAmount(kw, st, ch, start, end);
    }

    // Tổng lãi/lỗ theo bộ lọc = (lãi gộp toàn bộ dòng) - (tổng chiết khấu đơn)
    @Transactional(readOnly = true)
    public BigDecimal sumFilteredProfit(String keyword, String status, String channel,
                                        LocalDateTime start, LocalDateTime end) {
        String kw = (keyword == null || keyword.isBlank()) ? null
                : "%" + keyword.trim().toLowerCase() + "%";
        String st = (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) ? null : status;
        String ch = (channel == null || channel.isBlank() || "ALL".equalsIgnoreCase(channel)) ? null : channel;
        BigDecimal margin = orderRepository.sumFilteredMargin(kw, st, ch, start, end);
        BigDecimal discount = orderRepository.sumFilteredDiscount(kw, st, ch, start, end);
        if (margin == null) margin = BigDecimal.ZERO;
        if (discount == null) discount = BigDecimal.ZERO;
        return margin.subtract(discount);
    }

    // TẠO ĐƠN HÀNG MỚI
    @Transactional
    public Order createOrder(OrderRequest request) {
        String generatedCode = "DH" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        Customer customer = customerService.getCustomerByCode(request.getCustomerCode());

        String requestedStatus = request.getStatus() != null ? request.getStatus() : DRAFT;

        // CHỐNG MASS-ASSIGNMENT: chỉ cho phép đặt trực tiếp các trạng thái được phép khi TẠO đơn.
        // Không cho tạo thẳng đơn ở COMPLETED/CANCELLED/RETURNED hay trạng thái rác -> mọi chuyển tiếp
        // nhạy cảm phải đi qua updateOrder/changeStatus (có kiểm tra transition + trừ/nhả kho đúng).
        if (!CREATABLE_STATUSES.contains(requestedStatus)) {
            throw new BusinessException("Trạng thái tạo đơn không hợp lệ: " + requestedStatus);
        }

        // IDEMPOTENCY: nếu client gửi mã tham chiếu (đơn sàn / đồng bộ) và đã tồn tại đơn mang mã đó
        // -> coi như tạo trùng (double-submit / retry mạng) và chặn để không trừ kho + ghi thu 2 lần.
        String refCode = request.getReferenceCode();
        if (refCode != null && !refCode.isBlank() && orderRepository.existsByReferenceCode(refCode.trim())) {
            throw new BusinessException("Đơn hàng với mã tham chiếu [" + refCode + "] đã tồn tại, không tạo trùng.");
        }

        Order order = Order.builder().orderCode(generatedCode).customer(customer).salesChannelCode(request.getSalesChannelCode()).branchId(request.getBranchId()).status(requestedStatus).note(request.getNote()).shippingType(request.getShippingType()).shipFromBranchId(request.getShipFromBranchId()).shippingPartner(request.getShippingPartner()).trackingCode(request.getTrackingCode()).referenceCode(request.getReferenceCode()).shippingAddress(request.getShippingAddress()).expectedDeliveryDate(request.getExpectedDeliveryDate()).shippingFee(request.getShippingFee() != null ? request.getShippingFee() : BigDecimal.ZERO).codAmount(request.getCodAmount() != null ? request.getCodAmount() : BigDecimal.ZERO).shipWeight(request.getShipWeight()).paymentStatus(request.getPaymentStatus()).paymentMethod(request.getPaymentMethod()).amountPaid(request.getAmountPaid() != null ? request.getAmountPaid() : BigDecimal.ZERO).invoiceTaxCode(request.getInvoiceTaxCode()).invoiceCompanyName(request.getInvoiceCompanyName()).invoiceCompanyAddress(request.getInvoiceCompanyAddress()).discountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO).details(new ArrayList<>()).createdAt(request.getCreatedAt() != null ? request.getCreatedAt() : LocalDateTime.now()).build();

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
        notificationService.create("Đơn hàng mới", "Đơn hàng " + savedOrder.getOrderCode() + " vừa được tạo thành công.", Notification.NotificationType.ORDER, "/ui/orders/detail/" + savedOrder.getOrderCode());

        // 4. GHI SỔ QUỸ phần tiền khách đã trả khi tạo đơn (đơn mới -> tiền cũ = 0)
        syncSalesCashFlow(savedOrder, BigDecimal.ZERO, DRAFT, savedOrder.getAmountPaid(), savedOrder.getStatus());

        return savedOrder;
    }

    // CẬP NHẬT ĐƠN HÀNG
    @Transactional
    public Order updateOrder(String orderCode, OrderRequest request) {
        Order order = getOrderByCode(orderCode);
        String oldStatus = order.getStatus();
        BigDecimal oldPaid = order.getAmountPaid();
        String newStatus = request.getStatus();

        // Tự động nhảy sang Đã xác nhận nếu đang sửa từ Nháp -> Tạo đơn
        if (CREATED.equals(newStatus)) {
            newStatus = CONFIRMED;
        }

        // CHỐNG MASS-ASSIGNMENT: chỉ nhận trạng thái hợp lệ; không cho đặt RETURNED thủ công (đi qua module Trả hàng).
        if (newStatus == null || !ALL_STATUSES.contains(newStatus) || RETURNED.equals(newStatus)) {
            throw new BusinessException("Trạng thái không hợp lệ: " + newStatus);
        }

        if (CANCELLED.equals(oldStatus)) {
            throw new BusinessException("Không thể sửa đơn hàng đã bị hủy!");
        }
        if (RETURNED.equals(oldStatus)) {
            throw new BusinessException("Không thể sửa đơn hàng đã trả hàng! Hãy thao tác trên phiếu trả hàng.");
        }
        if (COMPLETED.equals(oldStatus) && !CANCELLED.equals(newStatus)) {
            throw new BusinessException("Đơn hàng đã hoàn thành chỉ có thể chuyển sang trạng thái Hủy!");
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

        // Giữ lại GIÁ VỐN LỊCH SỬ theo SKU trước khi build lại, để sửa đơn không ghi đè
        // COGS bằng giá vốn hiện tại (tránh bóp méo lợi nhuận đã chốt của đơn cũ).
        java.util.Map<String, BigDecimal> historicalCostBySku = new java.util.HashMap<>();
        for (OrderDetail oldDetail : order.getDetails()) {
            if ((oldDetail.getIsCustom() == null || !oldDetail.getIsCustom())
                    && oldDetail.getSku() != null && oldDetail.getCostPrice() != null) {
                historicalCostBySku.putIfAbsent(oldDetail.getSku(), oldDetail.getCostPrice());
            }
        }

        // Xóa Details cũ, build lại Details mới theo request sửa
        order.getDetails().clear();
        buildOrderDetailsAndCalculateTotal(order, request.getDetails());

        // Khôi phục giá vốn lịch sử cho các dòng đã tồn tại từ trước (dòng mới giữ giá vốn hiện tại)
        for (OrderDetail detail : order.getDetails()) {
            if ((detail.getIsCustom() == null || !detail.getIsCustom())
                    && historicalCostBySku.containsKey(detail.getSku())) {
                detail.setCostPrice(historicalCostBySku.get(detail.getSku()));
            }
        }

        if (!COMPLETED.equals(oldStatus) && COMPLETED.equals(newStatus)) {
            for (OrderDetail detail : order.getDetails()) {
                if (detail.getWarrantyMonths() != null && detail.getWarrantyMonths() > 0) {
                    detail.setWarrantyStartDate(LocalDateTime.now());
                    detail.setWarrantyEndDate(LocalDateTime.now().plusMonths(detail.getWarrantyMonths()));
                }
            }
        }

        // GHI LOG VÀ RUNG CHUÔNG NẾU ĐỔI TRẠNG THÁI
        BigDecimal requestPaid = request.getAmountPaid() != null ? request.getAmountPaid() : BigDecimal.ZERO;
        if (!oldStatus.equals(newStatus)) {
            addActivityLog(order, "Cập nhật trạng thái", "Chuyển trạng thái từ [" + translateStatus(oldStatus) + "] sang [" + translateStatus(newStatus) + "]");
            if (CANCELLED.equals(newStatus) || COMPLETED.equals(newStatus)) {
                String translatedStatus = translateStatus(newStatus);
                notificationService.create("Cập nhật đơn hàng " + order.getOrderCode(), "Đơn hàng đã chuyển sang trạng thái: " + translatedStatus, Notification.NotificationType.ORDER, "/ui/orders/detail/" + order.getOrderCode());
            }
        } else if ((oldPaid != null ? oldPaid : BigDecimal.ZERO).compareTo(requestPaid) != 0) {
            addActivityLog(order, "Thanh toán", "Cập nhật số tiền đã thanh toán: " + requestPaid + "đ");
        } else {
            addActivityLog(order, "Cập nhật thông tin", "Thay đổi thông tin chi tiết đơn hàng");
        }

        // ÁP DỤNG LẠI TỒN KHO MỚI
        applyInventory(order, newStatus);

        Order saved = orderRepository.save(order);

        // GHI SỔ QUỸ phần chênh lệch tiền đã thu sau khi sửa đơn (gồm cả trường hợp hủy -> đảo ngược tiền đã thu)
        syncSalesCashFlow(saved, oldPaid, oldStatus, saved.getAmountPaid(), newStatus);

        return saved;
    }

    /**
     * ĐỔI TRẠNG THÁI ĐƠN HÀNG (dùng cho cập nhật hàng loạt).
     * Chỉ thay đổi trạng thái, giữ nguyên các thông tin khác của đơn.
     * Mỗi đơn chạy trong 1 transaction riêng -> 1 đơn lỗi không làm hỏng các đơn còn lại.
     */
    @Transactional
    public Order changeStatus(String orderCode, String newStatus) {
        if (newStatus == null || newStatus.trim().isEmpty()) {
            throw new BusinessException("Trạng thái mới không hợp lệ!");
        }
        // CHỐNG MASS-ASSIGNMENT: chỉ nhận trạng thái nằm trong tập hợp lệ.
        // Không cho chuyển sang RETURNED qua đây (trả hàng đi qua module Trả hàng để nhả kho/hoàn tiền đúng).
        if (!ALL_STATUSES.contains(newStatus) || RETURNED.equals(newStatus)) {
            throw new BusinessException("Trạng thái không hợp lệ: " + newStatus);
        }
        Order order = getOrderByCode(orderCode);
        String oldStatus = order.getStatus();

        // Tạo đơn -> tự nhảy sang Đã xác nhận (đồng bộ với updateOrder)
        if (CREATED.equals(newStatus)) {
            newStatus = CONFIRMED;
        }

        if (oldStatus != null && oldStatus.equals(newStatus)) {
            throw new BusinessException("Đơn " + orderCode + " đã ở trạng thái [" + translateStatus(newStatus) + "]");
        }
        if (CANCELLED.equals(oldStatus)) {
            throw new BusinessException("Đơn " + orderCode + " đã bị hủy, không thể đổi trạng thái!");
        }
        if (COMPLETED.equals(oldStatus) && !CANCELLED.equals(newStatus)) {
            throw new BusinessException("Đơn " + orderCode + " đã hoàn thành, chỉ có thể chuyển sang Hủy!");
        }

        // Nhả kho theo trạng thái cũ rồi áp lại theo trạng thái mới
        revertInventory(order, oldStatus);
        order.setStatus(newStatus);

        // Kích hoạt bảo hành khi chuyển sang Hoàn thành
        if (!COMPLETED.equals(oldStatus) && COMPLETED.equals(newStatus)) {
            for (OrderDetail detail : order.getDetails()) {
                if (detail.getWarrantyMonths() != null && detail.getWarrantyMonths() > 0) {
                    detail.setWarrantyStartDate(LocalDateTime.now());
                    detail.setWarrantyEndDate(LocalDateTime.now().plusMonths(detail.getWarrantyMonths()));
                }
            }
        }

        addActivityLog(order, "Cập nhật trạng thái", "Chuyển trạng thái từ [" + translateStatus(oldStatus) + "] sang [" + translateStatus(newStatus) + "] (cập nhật hàng loạt)");

        applyInventory(order, newStatus);

        if (CANCELLED.equals(newStatus) || COMPLETED.equals(newStatus)) {
            notificationService.create("Cập nhật đơn hàng " + order.getOrderCode(), "Đơn hàng đã chuyển sang trạng thái: " + translateStatus(newStatus), Notification.NotificationType.ORDER, "/ui/orders/detail/" + order.getOrderCode());
        }

        Order saved = orderRepository.save(order);

        // Đổi trạng thái không đổi số tiền đã trả, nhưng "tiền hiệu lực" có thể đổi
        // (vd: Nháp->Xác nhận có tiền cọc -> ghi THU; *->Hủy -> đảo ngược tiền đã thu).
        syncSalesCashFlow(saved, saved.getAmountPaid(), oldStatus, saved.getAmountPaid(), newStatus);

        return saved;
    }

    // XÓA ĐƠN HÀNG
    @Transactional
    public void deleteOrder(String orderCode) {
        Order order = getOrderByCode(orderCode);
        // Không cho xóa đơn đã có phiếu trả hàng (FK nullable=false tham chiếu đơn gốc + tránh mất dấu vết/lệch số).
        if (RETURNED.equals(order.getStatus())) {
            throw new BusinessException("Đơn hàng đã trả hàng, không thể xóa. Hãy xử lý phiếu trả hàng trước.");
        }
        // Đảo ngược phần tiền đã ghi nhận vào Sổ quỹ trước khi xóa (coi như chuyển về trạng thái Hủy)
        syncSalesCashFlow(order, order.getAmountPaid(), order.getStatus(), order.getAmountPaid(), CANCELLED);
        revertInventory(order, order.getStatus());
        orderRepository.delete(order);
    }

    @Transactional(readOnly = true)
    public Order getOrderByCode(String orderCode) {
        return orderRepository.findByOrderCode(orderCode).orElseThrow(() -> new BusinessException("Không tìm thấy đơn hàng: " + orderCode));
    }

    // ============================================================
    // GHI SỔ QUỸ TỰ ĐỘNG CHO DÒNG TIỀN BÁN HÀNG
    // ------------------------------------------------------------
    // Mỗi khi số tiền KHÁCH ĐÃ TRẢ (amountPaid) của đơn thay đổi, ta ghi đúng phần CHÊNH LỆCH
    // vào Sổ quỹ: tăng -> phiếu THU, giảm/hủy -> phiếu CHI điều chỉnh. Nhờ vậy "Tiền quỹ" và
    // "Vốn thực" trên trang Quản lý vốn luôn phản ánh đủ tiền vào, đối xứng với phiếu nhập hàng
    // (vốn dĩ đã tự sinh phiếu CHI). Đơn Nháp/Hủy coi như chưa thu (tiền hiệu lực = 0).
    // ============================================================

    /** Số tiền được coi là đã thực thu theo trạng thái đơn (Nháp/Hủy = 0). */
    private BigDecimal effectivePaid(String status, BigDecimal paid) {
        if (status == null || DRAFT.equals(status) || CANCELLED.equals(status)) return BigDecimal.ZERO;
        return paid != null ? paid : BigDecimal.ZERO;
    }

    private CashTransaction.PaymentMethod mapPaymentMethod(String method) {
        if (method != null && (method.equalsIgnoreCase("TRANSFER") || method.equalsIgnoreCase("BANK"))) {
            return CashTransaction.PaymentMethod.BANK;
        }
        return CashTransaction.PaymentMethod.CASH;
    }

    /**
     * Ghi vào Sổ quỹ phần chênh lệch tiền đã thu của đơn giữa trạng thái cũ và mới.
     * Chạy trong cùng transaction với thao tác đơn hàng -> nếu đơn rollback thì phiếu quỹ cũng rollback (đảm bảo cân sổ).
     */
    private void syncSalesCashFlow(Order order, BigDecimal oldPaid, String oldStatus, BigDecimal newPaid, String newStatus) {
        BigDecimal oldEff = effectivePaid(oldStatus, oldPaid);
        BigDecimal newEff = effectivePaid(newStatus, newPaid);
        BigDecimal delta = newEff.subtract(oldEff);
        if (delta.signum() == 0) return;

        boolean isReceipt = delta.signum() > 0;
        CashTransactionRequest req = new CashTransactionRequest();
        req.setType(isReceipt ? CashTransaction.TransactionType.RECEIPT : CashTransaction.TransactionType.PAYMENT);
        req.setPaymentMethod(mapPaymentMethod(order.getPaymentMethod()));
        req.setTargetGroup(CashTransaction.TargetGroup.CUSTOMER);
        if (order.getCustomer() != null) req.setTargetId(order.getCustomer().getId());
        req.setAmount(delta.abs());
        req.setReason(isReceipt ? CashbookService.REASON_SALE_IN : CashbookService.REASON_SALE_ADJUST);
        req.setDescription((isReceipt ? "Thu tiền đơn hàng " : "Điều chỉnh/hoàn tiền đơn hàng ") + order.getOrderCode());
        req.setBranchId(order.getBranchId());
        req.setReferenceCode(order.getOrderCode());
        req.setTransactionDate(LocalDateTime.now());

        // notify = false: phiếu thu sinh tự động theo từng đơn -> KHÔNG rung chuông để tránh spam thông báo.
        cashbookService.createTransaction(req, false);
    }

    private void buildOrderDetailsAndCalculateTotal(Order order, List<OrderDetailRequest> detailRequests) {
        BigDecimal totalItemsAmount = BigDecimal.ZERO;

        for (OrderDetailRequest detailReq : detailRequests) {
            Product product = null;
            String productName = detailReq.getName();
            BigDecimal costPrice = BigDecimal.ZERO; // Khởi tạo biến Giá vốn

            if (detailReq.getIsCustom() == null || !detailReq.getIsCustom()) {
                try {
                    ProductVariant variant = variantRepository.findBySku(detailReq.getSku()).orElse(null);
                    if (variant != null) {
                        product = variant.getProduct();
                        String varName = variant.getVariantName();
                        productName = product.getName() + (varName != null && !varName.trim().isEmpty() ? " - " + varName : "");

                        // Lấy giá vốn chuẩn từ Database tại thời điểm tạo đơn
                        costPrice = variant.getCostPrice() != null ? variant.getCostPrice() : BigDecimal.ZERO;
                    } else {
                        productName = detailReq.getName();
                    }
                } catch (Exception e) {
                    // Không nuốt lỗi: nếu không lấy được biến thể sẽ làm sai giá vốn (COGS) -> dừng giao dịch
                    log.error("Lỗi khi lấy thông tin biến thể SKU {}", detailReq.getSku(), e);
                    throw new BusinessException("Không thể lấy thông tin sản phẩm SKU: " + detailReq.getSku());
                }
            } else {
                // Nếu là sản phẩm tùy chỉnh, lấy giá vốn gửi từ Request (thường là 0)
                costPrice = detailReq.getCostPrice() != null ? detailReq.getCostPrice() : BigDecimal.ZERO;
                // Chặn giá vốn âm gửi từ client (tránh COGS âm -> lợi nhuận khống)
                if (costPrice.compareTo(BigDecimal.ZERO) < 0) {
                    throw new BusinessException("Giá vốn sản phẩm tùy chỉnh không được âm!");
                }
            }

            BigDecimal unitPrice = detailReq.getUnitPrice() != null ? detailReq.getUnitPrice() : BigDecimal.ZERO;
            // Chống dữ liệu tiền âm gửi từ client
            if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException("Đơn giá sản phẩm [" + productName + "] không được âm!");
            }
            if (detailReq.getQuantity() <= 0) {
                throw new BusinessException("Số lượng sản phẩm [" + productName + "] phải lớn hơn 0!");
            }
            BigDecimal quantity = BigDecimal.valueOf(detailReq.getQuantity());
            BigDecimal lineTotal = unitPrice.multiply(quantity);

            OrderDetail orderDetail = OrderDetail.builder()
                    .order(order)
                    .product(product)
                    .sku(detailReq.getSku())
                    .productName(productName)
                    .quantity(detailReq.getQuantity())
                    .unitPrice(unitPrice)
                    .costPrice(costPrice)
                    .totalPrice(lineTotal)
                    .note(detailReq.getNote())
                    .isCustom(detailReq.getIsCustom() != null ? detailReq.getIsCustom() : false)
                    .serialNumber(detailReq.getSerialNumber())
                    .warrantyMonths(detailReq.getWarrantyMonths())
                    .build();

            order.getDetails().add(orderDetail);
            totalItemsAmount = totalItemsAmount.add(lineTotal);
        }

        BigDecimal discount = order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO;
        if (discount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Chiết khấu không được âm!");
        }
        // Chặn chiết khấu vượt quá tiền hàng + phí ship (tránh tổng đơn = 0 mà vẫn xuất hàng).
        // Bỏ qua kiểm tra này với đơn NHÁP để người dùng còn lưu nháp khi đang nhập dở.
        BigDecimal shippingFee = order.getShippingFee() != null ? order.getShippingFee() : BigDecimal.ZERO;
        BigDecimal maxDiscount = totalItemsAmount.add(shippingFee);
        if (!DRAFT.equals(order.getStatus()) && discount.compareTo(maxDiscount) > 0) {
            throw new BusinessException("Chiết khấu (" + discount + "đ) không được lớn hơn tổng tiền hàng + phí ship (" + maxDiscount + "đ)!");
        }
        order.setDiscountAmount(discount);

        BigDecimal finalTotal = totalItemsAmount.add(shippingFee).subtract(discount);
        order.setTotalAmount(finalTotal.compareTo(BigDecimal.ZERO) > 0 ? finalTotal : BigDecimal.ZERO);
    }

    public List<Order> findTop10ByCustomer_CodeOrderByCreatedAtDescByCode(String orderCode) {
        return orderRepository.findTop10ByCustomer_CodeOrderByCreatedAtDesc(orderCode);
    }

    // Trả về bản sao danh sách đã sắp xếp theo SKU để khóa hàng theo thứ tự cố định (chống deadlock)
    private List<OrderDetail> sortedBySku(List<OrderDetail> details) {
        List<OrderDetail> copy = new ArrayList<>(details);
        copy.sort(java.util.Comparator.comparing(d -> d.getSku() == null ? "" : d.getSku()));
        return copy;
    }

    /**
     * TRỪ KHO (Áp dụng khi Tạo đơn hoặc sau khi Sửa đơn)
     */
    private void applyInventory(Order order, String status) {
        if (CANCELLED.equals(status) || DRAFT.equals(status)) return;
        boolean isCompleted = COMPLETED.equals(status);

        Long branchId = order.getShipFromBranchId() != null ? order.getShipFromBranchId() : order.getBranchId();

        if (branchId == null) {
            throw new BusinessException("Không xác định được chi nhánh xuất hàng!");
        }

        // Khóa các dòng theo thứ tự SKU cố định để tránh deadlock giữa các giao dịch đồng thời
        for (OrderDetail detail : sortedBySku(order.getDetails())) {
            if (detail.getIsCustom() != null && detail.getIsCustom()) continue;

            ProductVariant variant = variantRepository.findBySkuForUpdate(detail.getSku()).orElseThrow(() -> new BusinessException("Không tìm thấy biến thể có mã SKU: " + detail.getSku()));

            // Ép văng lỗi nếu không có hàng tại chi nhánh này. Dùng khóa ghi để chống bán âm khi nhiều đơn cùng lúc.
            Inventory inv = inventoryRepository.findByVariantIdAndBranchIdForUpdate(variant.getId(), branchId).orElseThrow(() -> new BusinessException("Sản phẩm [" + detail.getProductName() + "] chưa từng được nhập vào chi nhánh này!"));

            if (inv.getAvailableStock() > inv.getStock()) {
                inv.setAvailableStock(inv.getStock());
            }

            // Kiểm tra xem có đủ hàng để bán không (luôn kiểm tra, kể cả khi hoàn thành đơn,
            // để không cho phép xuất quá tồn kho thực tế).
            if (inv.getAvailableStock() < detail.getQuantity()) {
                throw new BusinessException("Sản phẩm [" + detail.getProductName() + "] chỉ còn " + inv.getAvailableStock() + " chiếc có thể bán tại kho này!");
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

        for (OrderDetail detail : sortedBySku(order.getDetails())) {
            if (detail.getIsCustom() != null && detail.getIsCustom()) continue;

            ProductVariant variant = variantRepository.findBySkuForUpdate(detail.getSku()).orElse(null);
            if (variant != null) {
                // KHÔNG tạo bản ghi tồn kho mới khi nhả: nếu chi nhánh chưa từng có bản ghi cho SP này
                // thì việc apply trước đó cũng không thể trừ ở đây -> bỏ qua, tránh tạo tồn kho khống.
                Inventory inv = inventoryRepository.findByVariantIdAndBranchIdForUpdate(variant.getId(), branchId).orElse(null);
                if (inv == null) continue;

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
        OrderActivity activity = OrderActivity.builder().order(order).action(action).description(description).createdBy(getCurrentUser()).createdAt(LocalDateTime.now()).build();
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