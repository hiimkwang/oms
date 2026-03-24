package com.oms.module.customer.service;

import com.oms.module.customer.dto.CustomerRequest;
import com.oms.module.customer.entity.Customer;
import com.oms.module.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer getCustomerByCode(String customerCode) {
        return customerRepository.findByCustomerCode(customerCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với mã: " + customerCode));
    }

    public Customer createCustomer(CustomerRequest request) {
        if (customerRepository.existsByCustomerCode(request.getCustomerCode())) {
            throw new RuntimeException("Mã khách hàng đã tồn tại!");
        }

        Customer customer = Customer.builder()
                .customerCode(request.getCustomerCode())
                .fullName(request.getFullName())
                .company(request.getCompany())
                .address(request.getAddress())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .taxCode(request.getTaxCode())
                .build();

        return customerRepository.save(customer);
    }

    public Customer updateCustomer(String customerCode, CustomerRequest request) {
        Customer existingCustomer = getCustomerByCode(customerCode);

        existingCustomer.setFullName(request.getFullName());
        existingCustomer.setCompany(request.getCompany());
        existingCustomer.setAddress(request.getAddress());
        existingCustomer.setPhoneNumber(request.getPhoneNumber());
        existingCustomer.setEmail(request.getEmail());
        existingCustomer.setTaxCode(request.getTaxCode());

        return customerRepository.save(existingCustomer);
    }

    public void deleteCustomer(String customerCode) {
        Customer customer = getCustomerByCode(customerCode);
        customerRepository.delete(customer);
    }
}