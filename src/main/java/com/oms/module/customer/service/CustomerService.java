package com.oms.module.customer.service;

import com.oms.module.customer.dto.CustomerRequest;
import com.oms.module.customer.entity.Customer;
import com.oms.module.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;

    public List<CustomerRequest> getCustomerList(String keyword) {
        return customerRepository.findAllWithStats(keyword);
    }

    @Transactional
    public Customer createCustomer(CustomerRequest req) {
        if (customerRepository.existsByPhone(req.getPhoneNumber())) {
            throw new RuntimeException("Số điện thoại này đã tồn tại!");
        }

        Customer c = new Customer();
        // Tự sinh mã nếu không nhập
        c.setCode(req.getCustomerCode() != null && !req.getCustomerCode().isBlank()
                ? req.getCustomerCode() : "KH" + System.currentTimeMillis());
        c.setFullName(req.getFullName());
        c.setPhone(req.getPhoneNumber());
        c.setEmail(req.getEmail());
        c.setCompany(req.getCompanyName());
        c.setAddress(req.getCompanyAddress());
        c.setTaxCode(req.getTaxCode());
        c.setCustomerGroup(req.getCustomerGroup() != null ? req.getCustomerGroup() : "Khách lẻ");
        c.setNote(req.getNote());

        return customerRepository.save(c);
    }

    public Customer getCustomerByCode(String customerCode) {
        return  null;
    }
}