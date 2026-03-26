package com.oms.module.customer.service;

import com.oms.module.customer.dto.ConditionDTO;
import com.oms.module.customer.dto.CustomerRequest;
import com.oms.module.customer.dto.RuleDTO;
import com.oms.module.customer.entity.Customer;
import com.oms.module.customer.entity.CustomerGroup;
import com.oms.module.customer.repository.CustomerGroupRepository;
import com.oms.module.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerGroupRepository groupRepository;
    private final ObjectMapper objectMapper;

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

    @Transactional
    public void syncCustomerGroups() {
        // 1. Lấy toàn bộ khách hàng và toàn bộ nhóm
        List<Customer> customers = customerRepository.findAll();
        List<CustomerGroup> allGroups = groupRepository.findAll();

        // 2. Phân loại nhóm Tự động và lấy danh sách TÊN các nhóm Thủ công
        List<CustomerGroup> autoGroups = allGroups.stream()
                .filter(CustomerGroup::getAutoUpdate)
                .collect(Collectors.toList());

        List<String> manualGroupNames = allGroups.stream()
                .filter(g -> !g.getAutoUpdate())
                .map(CustomerGroup::getName)
                .collect(Collectors.toList());

        for (Customer customer : customers) {
            // NẾU KHÁCH ĐANG Ở NHÓM THỦ CÔNG -> BỎ QUA, KHÔNG RESET
            if (customer.getCustomerGroup() != null && manualGroupNames.contains(customer.getCustomerGroup())) {
                continue;
            }

            // Với khách lẻ hoặc khách ở nhóm tự động -> Đặt mốc mặc định là Khách lẻ
            String newGroupName = "Khách lẻ";

            // 3. Quét qua các điều kiện tự động
            for (CustomerGroup group : autoGroups) {
                if (group.getConditions() != null && !group.getConditions().isEmpty()) {
                    try {
                        ConditionDTO condObj = objectMapper.readValue(group.getConditions(), ConditionDTO.class);
                        if (evaluateCondition(customer, condObj)) {
                            newGroupName = group.getName();
                        }
                    } catch (Exception e) {
                        log.error("Lỗi parse điều kiện nhóm: {}", group.getName());
                    }
                }
            }

            // Cập nhật lại tên nhóm mới cho khách
            customer.setCustomerGroup(newGroupName);
        }

        // 4. Lưu lại toàn bộ
        customerRepository.saveAll(customers);
    }

    // Hàm phụ: Kiểm tra xem 1 khách hàng có thỏa mãn bộ điều kiện (JSON) không
    private boolean evaluateCondition(Customer customer, ConditionDTO condObj) {
        List<Boolean> results = new ArrayList<>();

        for (RuleDTO rule : condObj.getRules()) {
            double customerValue = 0;
            if ("ORDER_COUNT".equals(rule.getField())) {
                customerValue = customer.getOrderCount() != null ? customer.getOrderCount() : 0;
            } else if ("TOTAL_SPENT".equals(rule.getField())) {
                customerValue = customer.getTotalSpent() != null ? customer.getTotalSpent() : 0;
            }

            double targetValue = Double.parseDouble(rule.getValue());
            boolean match = false;

            switch (rule.getOperator()) {
                case ">":
                    match = customerValue > targetValue;
                    break;
                case "<":
                    match = customerValue < targetValue;
                    break;
                case "=":
                    match = customerValue == targetValue;
                    break;
            }
            results.add(match);
        }

        if ("ALL".equals(condObj.getMatchType())) {
            return !results.contains(false); // ALL: Phải đúng hết
        } else {
            return results.contains(true);   // ANY: Chỉ cần 1 cái đúng
        }
    }
}