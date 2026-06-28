package com.oms.module.reconciliation.service;

import com.oms.module.customer.dto.CustomerRequest;
import com.oms.module.customer.entity.Customer;
import com.oms.module.customer.repository.CustomerRepository;
import com.oms.module.customer.service.CustomerService;
import com.oms.module.order.dto.OrderDetailRequest;
import com.oms.module.order.dto.OrderRequest;
import com.oms.module.order.entity.Order;
import com.oms.module.order.repository.OrderRepository;
import com.oms.module.order.service.OrderService;
import com.oms.module.product.repository.ProductVariantRepository;
import com.oms.module.reconciliation.dto.ReconcileItem;
import com.oms.module.reconciliation.dto.ReconcileResult;
import com.oms.module.reconciliation.dto.ReconcileRow;
import com.oms.module.reconciliation.dto.ReconcileSyncRequest;
import com.oms.module.reconciliation.dto.ReconcileSyncResult;
import com.oms.module.setting.entity.SalesChannel;
import com.oms.module.setting.service.SalesChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Đối soát file của sàn TMĐT (Shopee/Lazada/TikTok) với đơn trên OMS và đồng bộ về OMS.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReconciliationService {

    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final CustomerService customerService;
    private final CustomerRepository customerRepository;
    private final ProductVariantRepository variantRepository;
    private final SalesChannelService salesChannelService;
    private final com.oms.module.cashbook.service.CashbookService cashbookService;

    private static final String[] TRACKING_KEYS = {"ma van don", "van don", "tracking", "waybill", "awb", "spx"};
    private static final String[] ORDERCODE_KEYS = {"ma don hang", "ma don", "order sn", "ordersn", "order id", "order no", "order number"};
    private static final String[] AMOUNT_PRIORITY = {
            "tong gia tri don hang", "tong so tien nguoi mua thanh toan", "nguoi mua thanh toan",
            "nguoi ban nhan", "tien thuc nhan", "tien ky quy",
            "thanh tien", "tong tien", "settlement", "payout", "amount", "thanh toan"
    };
    private static final String[] ITEMSKU_KEYS = {"sku phan loai", "sku san pham", "sku"};
    private static final String[] PRODNAME_KEYS = {"ten san pham", "ten sp", "product name"};
    private static final String[] VARNAME_KEYS = {"ten phan loai", "phan loai hang"};
    private static final String[] QTY_KEYS = {"so luong"};
    private static final String[] PRICE_KEYS = {"gia uu dai", "gia goc", "don gia", "gia ban"};
    private static final String[] RECIPIENT_KEYS = {"ten nguoi nhan", "nguoi nhan"};
    private static final String[] PHONE_KEYS = {"so dien thoai", "dien thoai", "sdt"};
    private static final String[] ADDRESS_KEYS = {"dia chi nhan hang", "dia chi"};
    private static final String[] STATUS_KEYS = {"trang thai don hang", "trang thai", "order status", "status"};
    private static final String[] BUYER_KEYS = {"nguoi mua", "buyer", "username", "tai khoan"};
    private static final String[] BUYERPAID_KEYS = {"tong so tien nguoi mua thanh toan", "nguoi mua thanh toan", "tong so tien thanh toan"};
    // Các khoản phí sàn trừ vào tiền người bán (Shopee)
    private static final String[] FEE_FIXED_KEYS = {"phi co dinh"};
    private static final String[] FEE_SERVICE_KEYS = {"phi dich vu"};
    private static final String[] FEE_TXN_KEYS = {"phi xu ly giao dich", "phi thanh toan"};

    public ReconcileResult reconcile(MultipartFile file, String channel, LocalDateTime start, LocalDateTime end) {
        try {
            List<String[]> rows = readFile(file);
            if (rows.isEmpty()) return failed("File rỗng hoặc không đọc được dữ liệu.");

            int headerIdx = findHeaderRow(rows);
            String[] header = rows.get(headerIdx);

            int idxTracking = findColumn(header, TRACKING_KEYS);
            int idxOrder = findColumn(header, ORDERCODE_KEYS);
            int idxAmount = findAmountColumn(header);
            int idxItemSku = findColumn(header, ITEMSKU_KEYS);
            int idxProdName = findColumn(header, PRODNAME_KEYS);
            int idxVarName = findColumn(header, VARNAME_KEYS);
            int idxQty = findColumn(header, QTY_KEYS);
            int idxPrice = findColumn(header, PRICE_KEYS);
            int idxRecipient = findColumn(header, RECIPIENT_KEYS);
            int idxPhone = findColumn(header, PHONE_KEYS);
            int idxAddress = findColumn(header, ADDRESS_KEYS);
            int idxStatus = findColumn(header, STATUS_KEYS);
            int idxFeeFixed = findColumn(header, FEE_FIXED_KEYS);
            int idxFeeService = findColumn(header, FEE_SERVICE_KEYS);
            int idxFeeTxn = findColumn(header, FEE_TXN_KEYS);
            // Người mua: ưu tiên cột tên CHÍNH XÁC "Người Mua" để không nhầm với "Tổng số tiền người mua thanh toán"
            int idxBuyer = findColumnExact(header, "nguoi mua");
            if (idxBuyer < 0) idxBuyer = findColumn(header, new String[]{"buyer", "username", "tai khoan"});
            int idxBuyerPaid = findColumn(header, BUYERPAID_KEYS);

            if (idxTracking < 0 && idxOrder < 0) {
                return failed("Không tìm thấy cột 'Mã đơn hàng' hoặc 'Mã vận đơn' trong file. Hãy kiểm tra lại file đối soát của sàn.");
            }

            String amountColumnName = idxAmount >= 0 ? header[idxAmount] : "(không tìm thấy cột số tiền)";
            Set<String> cancelledKeys = new HashSet<>();

            // Gộp các dòng theo đơn (Shopee xuất mỗi SP 1 dòng)
            Map<String, FileEntry> grouped = new LinkedHashMap<>();
            for (int i = headerIdx + 1; i < rows.size(); i++) {
                String[] r = rows.get(i);
                String tracking = cell(r, idxTracking);
                String orderCode = cell(r, idxOrder);
                if (tracking.isBlank() && orderCode.isBlank()) continue;

                String key = !orderCode.isBlank() ? "O:" + norm(orderCode) : "T:" + norm(tracking);

                // Loại đơn HỦY trên sàn khỏi đối soát
                String sanStatus = cell(r, idxStatus);
                if (isCancelledStatus(sanStatus)) {
                    cancelledKeys.add(key);
                    grouped.remove(key); // nếu trước đó đã thêm nhầm dòng của đơn này
                    continue;
                }
                if (cancelledKeys.contains(key)) continue;

                BigDecimal amount = idxAmount >= 0 ? parseAmount(cell(r, idxAmount)) : null;
                BigDecimal rowFee = nz(parseAmount(cell(r, idxFeeFixed)))
                        .add(nz(parseAmount(cell(r, idxFeeService))))
                        .add(nz(parseAmount(cell(r, idxFeeTxn))));

                FileEntry e = grouped.computeIfAbsent(key, k -> new FileEntry());
                if (e.sanStatus.isBlank() && !sanStatus.isBlank()) e.sanStatus = sanStatus;
                if (e.tracking.isBlank() && !tracking.isBlank()) e.tracking = tracking;
                if (e.orderCode.isBlank() && !orderCode.isBlank()) e.orderCode = orderCode;
                if (amount != null && amount.compareTo(e.amount) > 0) e.amount = amount;
                if (rowFee.compareTo(e.fee) > 0) e.fee = rowFee;
                if (e.customerName.isBlank()) e.customerName = cell(r, idxRecipient);
                if (e.customerPhone.isBlank()) e.customerPhone = cell(r, idxPhone);
                if (e.address.isBlank()) e.address = cell(r, idxAddress);
                if (e.buyerId.isBlank()) e.buyerId = cell(r, idxBuyer);
                BigDecimal bp = idxBuyerPaid >= 0 ? parseAmount(cell(r, idxBuyerPaid)) : null;
                if (bp != null && bp.compareTo(e.buyerPaid) > 0) e.buyerPaid = bp;

                // Dòng sản phẩm
                String itemSku = cell(r, idxItemSku);
                String pName = cell(r, idxProdName);
                String vName = cell(r, idxVarName);
                int qty = parseInt(cell(r, idxQty));
                BigDecimal price = idxPrice >= 0 ? parseAmount(cell(r, idxPrice)) : null;
                if (!itemSku.isBlank() || !pName.isBlank()) {
                    String fullName = pName + (vName != null && !vName.isBlank() ? " - " + vName : "");
                    e.items.add(ReconcileItem.builder()
                            .sku(itemSku).name(fullName.isBlank() ? itemSku : fullName)
                            .quantity(qty > 0 ? qty : 1)
                            .unitPrice(price != null ? price : BigDecimal.ZERO)
                            .build());
                }
            }

            // Bản đồ đơn OMS
            List<Order> omsOrders = orderRepository.findForReconciliation(start, end, channel);
            Map<String, Order> byTracking = new HashMap<>();
            Map<String, Order> byReference = new HashMap<>();
            for (Order o : omsOrders) {
                if (o.getTrackingCode() != null && !o.getTrackingCode().isBlank()) byTracking.put(norm(o.getTrackingCode()), o);
                if (o.getReferenceCode() != null && !o.getReferenceCode().isBlank()) byReference.put(norm(o.getReferenceCode()), o);
            }

            List<ReconcileRow> result = new ArrayList<>();
            Set<String> matchedOmsCodes = new HashSet<>();
            BigDecimal totalSan = BigDecimal.ZERO, totalOms = BigDecimal.ZERO, totalDiff = BigDecimal.ZERO;
            BigDecimal totalFee = BigDecimal.ZERO, totalNet = BigDecimal.ZERO;
            int matched = 0, missingOms = 0;

            for (FileEntry e : grouped.values()) {
                Order matchOrder = null;
                if (!e.orderCode.isBlank()) matchOrder = byReference.get(norm(e.orderCode));
                if (matchOrder == null && !e.tracking.isBlank()) matchOrder = byTracking.get(norm(e.tracking));
                if (matchOrder == null && !e.orderCode.isBlank()) matchOrder = byTracking.get(norm(e.orderCode));

                BigDecimal net = e.amount.subtract(e.fee);
                totalSan = totalSan.add(e.amount);
                totalFee = totalFee.add(e.fee);
                totalNet = totalNet.add(net);

                if (matchOrder != null) {
                    matchedOmsCodes.add(matchOrder.getOrderCode());
                    BigDecimal omsAmount = matchOrder.getTotalAmount() != null ? matchOrder.getTotalAmount() : BigDecimal.ZERO;
                    BigDecimal diff = e.amount.subtract(omsAmount);
                    totalOms = totalOms.add(omsAmount);
                    totalDiff = totalDiff.add(diff);
                    matched++;
                    result.add(ReconcileRow.builder()
                            .status("MATCHED").statusLabel("Khớp")
                            .tracking(e.tracking).sanOrderCode(e.orderCode).sanStatus(e.sanStatus)
                            .buyerId(e.buyerId).buyerPaid(e.buyerPaid).omsOrderCode(matchOrder.getOrderCode())
                            .sanAmount(e.amount).fee(e.fee).netReceived(net).omsAmount(omsAmount).diff(diff)
                            .note(diff.signum() == 0 ? "Khớp số tiền" : "")
                            .build());
                } else {
                    missingOms++;
                    result.add(ReconcileRow.builder()
                            .status("MISSING_OMS").statusLabel("Chưa có trên OMS")
                            .tracking(e.tracking).sanOrderCode(e.orderCode).sanStatus(e.sanStatus)
                            .buyerId(e.buyerId).buyerPaid(e.buyerPaid).omsOrderCode(null)
                            .sanAmount(e.amount).fee(e.fee).netReceived(net).omsAmount(null).diff(null)
                            .note("Đơn có trên sàn nhưng chưa tạo trên OMS — có thể tạo bù bên dưới")
                            .customerName(e.customerName)
                            .customerPhone(isValidPhone(e.customerPhone) ? e.customerPhone : e.buyerId)
                            .shippingAddress(e.address)
                            .items(e.items)
                            .build());
                }
            }

            int missingFile = 0;
            for (Order o : omsOrders) {
                if (!matchedOmsCodes.contains(o.getOrderCode())) {
                    missingFile++;
                    BigDecimal omsAmount = o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO;
                    result.add(ReconcileRow.builder()
                            .status("MISSING_FILE").statusLabel("Chưa đối soát")
                            .tracking(o.getTrackingCode()).sanOrderCode(o.getReferenceCode()).omsOrderCode(o.getOrderCode())
                            .sanAmount(null).omsAmount(omsAmount).diff(null)
                            .note("Đơn trên OMS nhưng không thấy trong file sàn — có thể sàn chưa thanh toán")
                            .build());
                }
            }

            return ReconcileResult.builder()
                    .success(true).message("Đối soát hoàn tất.")
                    .amountColumnName(amountColumnName)
                    .excludedCancelled(cancelledKeys.size())
                    .totalFileRows(grouped.size())
                    .matchedCount(matched).missingOmsCount(missingOms).missingFileCount(missingFile)
                    .totalSanAmount(totalSan).totalFee(totalFee).totalNetReceived(totalNet)
                    .totalOmsAmount(totalOms).totalDiff(totalDiff)
                    .rows(result)
                    .build();
        } catch (Exception ex) {
            log.error("Lỗi đối soát: {}", ex.getMessage(), ex);
            return failed("Lỗi khi đọc file: " + ex.getMessage());
        }
    }

    // ===== ĐỒNG BỘ VỀ OMS =====
    public ReconcileSyncResult sync(ReconcileSyncRequest req) {
        List<String> errors = new ArrayList<>();
        int created = 0, filled = 0, paid = 0;

        String status = (req.getCreateStatus() != null && !req.getCreateStatus().isBlank()) ? req.getCreateStatus() : "DRAFT";
        String channelCode = resolveChannel(req.getChannel());

        // 1. Tạo đơn bù
        if (req.getCreateOrders() != null) {
            for (ReconcileSyncRequest.CreateOrder co : req.getCreateOrders()) {
                try {
                    created += createMissingOrder(co, channelCode, status) ? 1 : 0;
                } catch (Exception e) {
                    errors.add("Tạo đơn " + safe(co.getReferenceCode()) + ": " + e.getMessage());
                }
            }
        }

        // 2. Điền mã đơn sàn / mã vận đơn cho đơn đã khớp
        if (req.getFillCodes() != null) {
            for (ReconcileSyncRequest.FillCode fc : req.getFillCodes()) {
                try {
                    Order o = orderRepository.findByOrderCode(fc.getOmsOrderCode()).orElse(null);
                    if (o == null) { errors.add("Không thấy đơn " + fc.getOmsOrderCode()); continue; }
                    boolean changed = false;
                    if ((o.getReferenceCode() == null || o.getReferenceCode().isBlank()) && fc.getReferenceCode() != null && !fc.getReferenceCode().isBlank()) {
                        o.setReferenceCode(fc.getReferenceCode()); changed = true;
                    }
                    if ((o.getTrackingCode() == null || o.getTrackingCode().isBlank()) && fc.getTrackingCode() != null && !fc.getTrackingCode().isBlank()) {
                        o.setTrackingCode(fc.getTrackingCode()); changed = true;
                    }
                    if (changed) { orderRepository.save(o); filled++; }
                } catch (Exception e) {
                    errors.add("Điền mã đơn " + fc.getOmsOrderCode() + ": " + e.getMessage());
                }
            }
        }

        // 3. Đánh dấu đã thanh toán
        if (req.getMarkPaidOrderCodes() != null) {
            for (String code : req.getMarkPaidOrderCodes()) {
                try {
                    Order o = orderRepository.findByOrderCode(code).orElse(null);
                    if (o == null) { errors.add("Không thấy đơn " + code); continue; }
                    BigDecimal oldPaid = o.getAmountPaid() != null ? o.getAmountPaid() : BigDecimal.ZERO;
                    BigDecimal total = o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO;
                    o.setPaymentStatus("PAID");
                    o.setAmountPaid(total);
                    orderRepository.save(o);

                    // Ghi phiếu THU vào sổ quỹ cho phần tiền mới thu (đối xứng với đơn bán thường)
                    BigDecimal delta = total.subtract(oldPaid);
                    if (delta.compareTo(BigDecimal.ZERO) > 0) {
                        cashbookService.recordSaleReceipt(
                                o.getCustomer() != null ? o.getCustomer().getId() : null,
                                o.getOrderCode(), o.getBranchId(), o.getPaymentMethod(), delta);
                    }
                    paid++;
                } catch (Exception e) {
                    errors.add("Thanh toán đơn " + code + ": " + e.getMessage());
                }
            }
        }

        return ReconcileSyncResult.builder()
                .success(true).createdCount(created).filledCount(filled).paidCount(paid).errors(errors).build();
    }

    private boolean createMissingOrder(ReconcileSyncRequest.CreateOrder co, String channelCode, String status) {
        String customerCode = resolveCustomerCode(co.getCustomerName(), co.getCustomerPhone(), co.getShippingAddress());

        List<OrderDetailRequest> details = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        if (co.getItems() != null) {
            for (ReconcileItem it : co.getItems()) {
                OrderDetailRequest d = new OrderDetailRequest();
                boolean known = it.getSku() != null && !it.getSku().isBlank()
                        && variantRepository.findBySku(it.getSku()).isPresent();
                d.setSku((it.getSku() != null && !it.getSku().isBlank()) ? it.getSku() : ("SAN_" + System.nanoTime()));
                d.setName(it.getName() != null && !it.getName().isBlank() ? it.getName() : "Sản phẩm sàn");
                d.setQuantity(it.getQuantity() > 0 ? it.getQuantity() : 1);
                d.setUnitPrice(it.getUnitPrice() != null ? it.getUnitPrice() : BigDecimal.ZERO);
                d.setIsCustom(!known); // SKU không khớp kho -> dòng tùy chỉnh để không vỡ tồn kho
                d.setCostPrice(BigDecimal.ZERO);
                details.add(d);
                total = total.add(d.getUnitPrice().multiply(BigDecimal.valueOf(d.getQuantity())));
            }
        }
        if (details.isEmpty()) {
            OrderDetailRequest d = new OrderDetailRequest();
            d.setSku("SAN_" + System.nanoTime());
            d.setName("Đơn sàn " + safe(co.getReferenceCode()));
            d.setQuantity(1);
            d.setUnitPrice(BigDecimal.ZERO);
            d.setIsCustom(true);
            d.setCostPrice(BigDecimal.ZERO);
            details.add(d);
        }

        OrderRequest req = new OrderRequest();
        req.setCustomerCode(customerCode);
        req.setSalesChannelCode(channelCode);
        req.setStatus(status);
        req.setPaymentStatus("UNPAID");
        req.setShippingType("PLATFORM");
        req.setReferenceCode(co.getReferenceCode());
        req.setTrackingCode(co.getTrackingCode());
        req.setShippingAddress(co.getShippingAddress());
        req.setDiscountAmount(BigDecimal.ZERO);
        req.setShippingFee(BigDecimal.ZERO);
        req.setTotalAmount(total);
        req.setNote("Tạo từ đối soát sàn");
        req.setDetails(details);

        orderService.createOrder(req);
        return true;
    }

    private String resolveCustomerCode(String name, String phone, String address) {
        if (phone != null && !phone.isBlank()) {
            for (Customer c : customerRepository.findByFullNameContainingIgnoreCaseOrPhoneContaining(phone, phone)) {
                if (phone.equals(c.getPhone())) return c.getCode();
            }
            CustomerRequest req = new CustomerRequest();
            req.setFullName(name != null && !name.isBlank() ? name : "Khách Shopee");
            req.setPhoneNumber(phone);
            req.setShipAddressDetail(address);
            req.setCustomerGroup("Khách lẻ");
            return customerService.createCustomer(req).getCode();
        }
        // Không có SĐT -> dùng khách chung "Khách sàn TMĐT"
        return customerRepository.findByCode("KH-SAN").map(Customer::getCode).orElseGet(() -> {
            CustomerRequest req = new CustomerRequest();
            req.setCustomerCode("KH-SAN");
            req.setFullName("Khách sàn TMĐT");
            req.setCustomerGroup("Khách lẻ");
            return customerService.createCustomer(req).getCode();
        });
    }

    private String resolveChannel(String channel) {
        if (channel != null && !channel.isBlank() && !"ALL".equalsIgnoreCase(channel)) return channel;
        // Tìm kênh có tên/mã chứa "shopee"
        try {
            for (SalesChannel sc : salesChannelService.findAll()) {
                String n = unaccent((sc.getName() != null ? sc.getName() : "") + " " + (sc.getCode() != null ? sc.getCode() : ""));
                if (n.contains("shopee")) return sc.getCode();
            }
        } catch (Exception ignored) {}
        return "SHOPEE";
    }

    private String safe(String s) {
        return s != null ? s : "";
    }

    // Trạng thái sàn là "đã hủy" -> loại khỏi đối soát
    private boolean isCancelledStatus(String status) {
        if (status == null || status.isBlank()) return false;
        String s = unaccent(status);
        return s.contains("huy") || s.contains("cancel");
    }

    private ReconcileResult failed(String msg) {
        return ReconcileResult.builder().success(false).message(msg).rows(new ArrayList<>()).build();
    }

    // ===== ĐỌC FILE =====
    private List<String[]> readFile(MultipartFile file) throws Exception {
        String name = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        if (name.endsWith(".xlsx") || name.endsWith(".xls")) return readExcel(file);
        return readCsv(file);
    }

    private List<String[]> readExcel(MultipartFile file) throws Exception {
        List<String[]> rows = new ArrayList<>();
        DataFormatter fmt = new DataFormatter();
        try (InputStream is = file.getInputStream(); Workbook wb = new XSSFWorkbook(is)) {
            Sheet sheet = wb.getSheetAt(0);
            int lastCol = 0;
            for (Row row : sheet) lastCol = Math.max(lastCol, row.getLastCellNum());
            for (Row row : sheet) {
                String[] arr = new String[lastCol < 0 ? 0 : lastCol];
                for (int c = 0; c < arr.length; c++) {
                    Cell cell = row.getCell(c);
                    arr[c] = cell != null ? fmt.formatCellValue(cell).trim() : "";
                }
                rows.add(arr);
            }
        }
        return rows;
    }

    private List<String[]> readCsv(MultipartFile file) throws Exception {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                rows.add(splitCsv(line));
            }
        }
        return rows;
    }

    private String[] splitCsv(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') inQuotes = !inQuotes;
            else if (ch == ',' && !inQuotes) { out.add(sb.toString().trim()); sb.setLength(0); }
            else sb.append(ch);
        }
        out.add(sb.toString().trim());
        return out.toArray(new String[0]);
    }

    // ===== NHẬN DIỆN CỘT =====
    private int findHeaderRow(List<String[]> rows) {
        int limit = Math.min(15, rows.size());
        for (int i = 0; i < limit; i++) {
            if (findColumn(rows.get(i), ORDERCODE_KEYS) >= 0 || findColumn(rows.get(i), TRACKING_KEYS) >= 0) return i;
        }
        return 0;
    }

    private int findColumn(String[] header, String[] keys) {
        for (String k : keys) {
            for (int i = 0; i < header.length; i++) {
                if (unaccent(header[i]).contains(k)) return i;
            }
        }
        return -1;
    }

    // Tìm cột có tiêu đề KHỚP CHÍNH XÁC (sau khi bỏ dấu) - tránh nhầm cột chứa cùng từ khóa
    private int findColumnExact(String[] header, String key) {
        for (int i = 0; i < header.length; i++) {
            if (unaccent(header[i]).equals(key)) return i;
        }
        return -1;
    }

    // SĐT hợp lệ: không bị che (*), đủ chữ số
    private boolean isValidPhone(String phone) {
        if (phone == null || phone.isBlank() || phone.contains("*")) return false;
        return phone.replaceAll("[^0-9]", "").length() >= 8;
    }

    private int findAmountColumn(String[] header) {
        for (String k : AMOUNT_PRIORITY) {
            for (int i = 0; i < header.length; i++) {
                String h = unaccent(header[i]);
                if (!h.contains(k)) continue;
                if (h.contains("thoi gian") || h.contains("ngay") || h.contains("tro gia")
                        || h.startsWith("phi") || h.contains("hoan")) continue;
                return i;
            }
        }
        return -1;
    }

    private String cell(String[] row, int idx) {
        if (idx < 0 || row == null || idx >= row.length) return "";
        return row[idx] != null ? row[idx].trim() : "";
    }

    private String norm(String s) {
        return s == null ? "" : s.trim().toUpperCase().replaceAll("\\s+", "");
    }

    private String unaccent(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s.toLowerCase().trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return n.replace("đ", "d");
    }

    private int parseInt(String s) {
        if (s == null) return 0;
        String d = s.replaceAll("[^0-9]", "");
        if (d.isEmpty()) return 0;
        try { return Integer.parseInt(d); } catch (Exception e) { return 0; }
    }

    private BigDecimal parseAmount(String s) {
        if (s == null || s.isBlank()) return null;
        boolean neg = s.contains("-") || s.contains("(");
        String t = s.replaceAll("[^0-9.,]", "");
        if (t.isEmpty()) return null;
        int decPos = Math.max(t.lastIndexOf('.'), t.lastIndexOf(','));
        String intPart = t;
        if (decPos >= 0) {
            String afterDec = t.substring(decPos + 1);
            if (afterDec.length() >= 1 && afterDec.length() <= 2 && afterDec.matches("\\d+")) {
                intPart = t.substring(0, decPos);
            }
        }
        String digits = intPart.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return BigDecimal.ZERO;
        BigDecimal v = new BigDecimal(digits);
        return neg ? v.negate() : v;
    }

    private BigDecimal nz(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private static class FileEntry {
        String tracking = "";
        String orderCode = "";
        String sanStatus = "";
        String buyerId = "";
        BigDecimal amount = BigDecimal.ZERO;
        BigDecimal fee = BigDecimal.ZERO;
        BigDecimal buyerPaid = BigDecimal.ZERO;
        String customerName = "";
        String customerPhone = "";
        String address = "";
        List<ReconcileItem> items = new ArrayList<>();
    }
}
