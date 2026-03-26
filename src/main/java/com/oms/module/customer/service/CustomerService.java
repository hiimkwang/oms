package com.oms.module.customer.service;

import com.oms.module.customer.dto.CustomerRequest;
import com.oms.module.customer.entity.Customer;
import com.oms.module.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;

    public List<CustomerRequest> getCustomerList(String keyword) {
        List<Customer> customers = customerRepository.searchByKeyword(keyword);

        return customers.stream().map(c -> {
            CustomerRequest req = new CustomerRequest();
            // Đảm bảo không bị lỗi nếu DB đang có dữ liệu rác (null)
            req.setCustomerCode(c.getCode() != null ? c.getCode() : "UNKNOWN");
            req.setFullName(c.getFullName() != null ? c.getFullName() : "Khách chưa có tên");
            req.setPhoneNumber(c.getPhone() != null ? c.getPhone() : "");
            req.setEmail(c.getEmail() != null ? c.getEmail() : "");
            req.setCustomerGroup(c.getCustomerGroup() != null ? c.getCustomerGroup() : "Khách lẻ");

            req.setOrderCount(0L);
            req.setTotalSpent(0.0);
            return req;
        }).collect(Collectors.toList());
    }

    @Transactional
    public Customer createCustomer(CustomerRequest req) {
        if (customerRepository.existsByPhone(req.getPhoneNumber())) {
            throw new RuntimeException("Số điện thoại này đã tồn tại!");
        }

        Customer c = new Customer();
        c.setCode(req.getCustomerCode() != null && !req.getCustomerCode().isBlank()
                ? req.getCustomerCode() : "KH" + System.currentTimeMillis());

        mapDtoToEntity(req, c);
        return customerRepository.save(c);
    }

    @Transactional
    public Customer updateCustomer(String code, CustomerRequest req) {
        Customer c = customerRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng: " + code));

        // Kiểm tra trùng SĐT nếu có đổi SĐT
        if (!c.getPhone().equals(req.getPhoneNumber()) && customerRepository.existsByPhone(req.getPhoneNumber())) {
            throw new RuntimeException("Số điện thoại này đã được sử dụng!");
        }

        mapDtoToEntity(req, c);
        return customerRepository.save(c);
    }

    public Customer getCustomerByCode(String customerCode) {
        return customerRepository.findByCode(customerCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng: " + customerCode));
    }

    // Hàm phụ trợ map dữ liệu
    private void mapDtoToEntity(CustomerRequest req, Customer c) {
        c.setFirstName(req.getFirstName());
        c.setLastName(req.getLastName());
        c.setFullName(req.getFullName());
        c.setPhone(req.getPhoneNumber());
        c.setEmail(req.getEmail());
        c.setDob(req.getDob());
        c.setGender(req.getGender());
        c.setCustomerGroup(req.getCustomerGroup() != null ? req.getCustomerGroup() : "Khách lẻ");
        c.setNote(req.getNote());
        c.setTags(req.getTags());

        c.setShipCompany(req.getShipCompany());
        c.setShipCity(req.getShipCity());
        c.setShipDistrict(req.getShipDistrict());
        c.setShipAddressDetail(req.getShipAddressDetail());

        c.setHasInvoice(req.getHasInvoice() != null ? req.getHasInvoice() : false);
        c.setTaxCode(req.getTaxCode());
        c.setCompanyName(req.getCompanyName());
        c.setCompanyAddress(req.getCompanyAddress());
    }
    @Transactional
    public void deleteCustomer(String code) {
        Customer c = customerRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng: " + code));
        customerRepository.delete(c);
    }
    @Transactional
    public void bulkDeleteCustomers(List<String> codes) {
        if (codes != null && !codes.isEmpty()) {
            customerRepository.deleteAllByCodeIn(codes);
        }
    }
}