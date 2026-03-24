package com.oms.module.quotation.service;

import com.oms.module.customer.entity.Customer;
import com.oms.module.customer.service.CustomerService;
import com.oms.module.product.entity.Product;
import com.oms.module.product.service.ProductService;
import com.oms.module.quotation.dto.QuotationDetailRequest;
import com.oms.module.quotation.dto.QuotationRequest;
import com.oms.module.quotation.entity.Quotation;
import com.oms.module.quotation.entity.QuotationDetail;
import com.oms.module.quotation.repository.QuotationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuotationService {

    private final QuotationRepository quotationRepository;
    private final CustomerService customerService;
    private final ProductService productService;

    public List<Quotation> getAllQuotations() {
        return quotationRepository.findAll();
    }

    public Quotation getQuotationByCode(String quotationCode) {
        return quotationRepository.findByQuotationCode(quotationCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy báo giá: " + quotationCode));
    }

    @Transactional
    public Quotation createQuotation(QuotationRequest request) {
        if (quotationRepository.existsByQuotationCode(request.getQuotationCode())) {
            throw new RuntimeException("Số báo giá đã tồn tại: " + request.getQuotationCode());
        }

        Customer customer = customerService.getCustomerByCode(request.getCustomerCode());

        Quotation quotation = Quotation.builder()
                .quotationCode(request.getQuotationCode())
                .quotationDate(request.getQuotationDate())
                .validUntil(request.getValidUntil())
                .staffName(request.getStaffName())
                .customer(customer)
                .status(request.getStatus())
                .taxPercent(request.getTaxPercent())
                .note(request.getNote())
                .quotationDetails(new ArrayList<>())
                .build();

        double totalAmount = 0.0;

        for (QuotationDetailRequest detailReq : request.getDetails()) {
            Product product = productService.getProductBySku(detailReq.getSku());

            // Lấy giá bán đề xuất nếu người dùng không nhập giá cụ thể
            double unitPrice = detailReq.getUnitPrice() != null ? detailReq.getUnitPrice() : Double.valueOf(String.valueOf(product.getPrice()));
            double totalPrice = unitPrice * detailReq.getQuantity();

            QuotationDetail detail = QuotationDetail.builder()
                    .quotation(quotation)
                    .product(product)
                    .quantity(detailReq.getQuantity())
                    .unitPrice(unitPrice)
                    .totalPrice(totalPrice)
                    .warranty(detailReq.getWarranty() != null ? detailReq.getWarranty() : "Theo quy định")
                    .build();

            quotation.getQuotationDetails().add(detail);
            totalAmount += totalPrice;
        }

        quotation.setTotalAmount(totalAmount);

        // Tính tổng tiền sau thuế
        double grandTotal = totalAmount + (totalAmount * (request.getTaxPercent() / 100));
        quotation.setGrandTotal(grandTotal);

        return quotationRepository.save(quotation);
    }

    @Transactional
    public Quotation updateQuotationStatus(String quotationCode, String status) {
        Quotation quotation = getQuotationByCode(quotationCode);
        quotation.setStatus(status);
        return quotationRepository.save(quotation);
    }

    public void deleteQuotation(String quotationCode) {
        Quotation quotation = getQuotationByCode(quotationCode);
        quotationRepository.delete(quotation);
    }
}