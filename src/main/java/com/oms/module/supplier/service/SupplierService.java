package com.oms.module.supplier.service;

import com.oms.module.receipt.dto.SupplierStatsResponse;
import com.oms.module.receipt.entity.Receipt;
import com.oms.module.receipt.repository.ReceiptRepository;
import com.oms.module.supplier.dto.SupplierRequest;
import com.oms.module.supplier.entity.Supplier;
import com.oms.module.supplier.enums.SupplierStatus;
import com.oms.module.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;

    private final ReceiptRepository receiptRepository;
    @Transactional(readOnly = true)
    public List<Supplier> getSuppliers(String keyword) {
        return supplierRepository.searchSuppliers(keyword);
    }

    @Transactional(readOnly = true)
    public Supplier getSupplierByCode(String code) {
        // Cần thêm hàm findByCode trong SupplierRepository nhé
        return supplierRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Nhà cung cấp với mã: " + code));
    }

    @Transactional
    public Supplier createSupplier(SupplierRequest request) {
        if (request.getCode() != null && !request.getCode().trim().isEmpty()) {
            if (supplierRepository.existsByCode(request.getCode())) {
                throw new RuntimeException("Mã nhà cung cấp đã tồn tại!");
            }
        }

        Supplier supplier = new Supplier();
        mapRequestToEntity(request, supplier);
        supplier.setStatus(SupplierStatus.ACTIVE);

        return supplierRepository.save(supplier);
    }

    @Transactional
    public Supplier updateSupplier(String code, SupplierRequest request) {
        Supplier supplier = getSupplierByCode(code);
        mapRequestToEntity(request, supplier);
        return supplierRepository.save(supplier);
    }

    // Hàm phụ trợ map DTO sang Entity
    private void mapRequestToEntity(SupplierRequest request, Supplier supplier) {
        //supplier.setCode(request.getCode());
        supplier.setName(request.getName());
        supplier.setPhone(request.getPhone());
        supplier.setEmail(request.getEmail());
        supplier.setTaxCode(request.getTaxCode());
        supplier.setWebsite(request.getWebsite());
        supplier.setFax(request.getFax());
        supplier.setCountry(request.getCountry());
        supplier.setProvince(request.getProvince());
        supplier.setDistrict(request.getDistrict());
        supplier.setWard(request.getWard());
        supplier.setAddressDetail(request.getAddressDetail());
        supplier.setAssignee(request.getAssignee());
        supplier.setTags(request.getTags());
    }

    @Transactional
    public void bulkDeleteByCodes(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return;
        }
        supplierRepository.deleteAllByCodeIn(codes);
    }

    // Nhớ inject ReceiptRepository vào nhé: private final ReceiptRepository receiptRepository;

    public SupplierStatsResponse getSupplierStats(String code, LocalDateTime start, LocalDateTime end) {
        // 1. Đếm số đơn và tổng tiền
        Object[] basicStats = receiptRepository.getBasicStats(code, start, end);
        long count = 0L;
        BigDecimal total = BigDecimal.ZERO;

        if (basicStats != null && basicStats.length > 0 && basicStats[0] != null) {
            Object[] statsResult = (Object[]) basicStats[0];
            count = (long) (statsResult[0] != null ? statsResult[0] : 0L);
            total = (BigDecimal) (statsResult[1] != null ? statsResult[1] : BigDecimal.ZERO);
        }

        // 2. Lấy tổng nợ
        BigDecimal debt = receiptRepository.getTotalDebt(code, start, end);
        if (debt == null) debt = BigDecimal.ZERO;

        // 3. Lấy lịch sử giao dịch
        List<Receipt> receipts = receiptRepository.findBySupplierCodeAndCreatedAtBetweenOrderByCreatedAtDesc(code, start, end);
        List<SupplierStatsResponse.ReceiptSummary> history = receipts.stream()
                .map(r -> SupplierStatsResponse.ReceiptSummary.builder()
                        .code(r.getCode())
                        .createdAt(r.getCreatedAt())
                        .status(r.getStatus())
                        .paymentStatus(r.getPaymentStatus())
                        .totalAmount(r.getTotalAmount())
                        .build())
                .collect(Collectors.toList());

        return SupplierStatsResponse.builder()
                .totalOrders(count)
                .totalAmount(total)
                .totalDebt(debt)
                .history(history)
                .build();
    }
    @Transactional
    public void updateStatus(String code, String status) {
        // 1. Tìm nhà cung cấp theo Mã NCC
        Supplier supplier = supplierRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Nhà cung cấp với mã: " + code));

        // 2. Cập nhật trạng thái
        try {
            supplier.setStatus(SupplierStatus.valueOf(status.toUpperCase()));

        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Trạng thái gửi lên không hợp lệ: " + status);
        }

        // 3. Lưu lại vào DB
        supplierRepository.save(supplier);
    }
}