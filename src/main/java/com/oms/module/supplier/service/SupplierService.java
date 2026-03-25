package com.oms.module.supplier.service;

import com.oms.module.supplier.dto.SupplierRequest;
import com.oms.module.supplier.entity.Supplier;
import com.oms.module.supplier.enums.SupplierStatus;
import com.oms.module.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;

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
        supplier.setCode(request.getCode());
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
}