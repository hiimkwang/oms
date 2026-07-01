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

            // --- 1. Thông tin cơ bản ---
            req.setCustomerCode(c.getCode() != null ? c.getCode() : "UNKNOWN");
            req.setFirstName(c.getFirstName());
            req.setLastName(c.getLastName());
            req.setFullName(c.getFullName() != null ? c.getFullName() : "Khách chưa có tên");
            req.setPhoneNumber(c.getPhone() != null ? c.getPhone() : "");
            req.setEmail(c.getEmail() != null ? c.getEmail() : "");
            req.setDob(c.getDob());
            req.setGender(c.getGender());

            // --- 2. Phân loại & Ghi chú ---
            req.setCustomerGroup(c.getCustomerGroup() != null ? c.getCustomerGroup() : "Khách lẻ");
            req.setNote(c.getNote());
            req.setTags(c.getTags());

            // --- 3. Địa chỉ nhận hàng (Shipping) ---
            req.setShipFirstName(c.getFirstName());
            req.setShipLastName(c.getLastName());
            req.setShipPhone(c.getPhone());
            req.setShipCompany(c.getShipCompany());
            req.setShipCity(c.getShipCity());
            req.setShipDistrict(c.getShipDistrict());
            req.setShipAddressDetail(c.getShipAddressDetail());

            // --- 4. Thông tin hóa đơn (VAT) ---
            req.setHasInvoice(c.getHasInvoice() != null ? c.getHasInvoice() : false);
            req.setTaxCode(c.getTaxCode());
            req.setCompanyName(c.getCompanyName());
            req.setCompanyAddress(c.getCompanyAddress());

            // --- 5. Thông tin thống kê ---
            req.setOrderCount(c.getOrderCount() != null ? c.getOrderCount().longValue() : 0L);
            req.setTotalSpent(c.getTotalSpent() != null ? c.getTotalSpent() : java.math.BigDecimal.ZERO);

            return req;
        }).collect(Collectors.toList());
    }

    @Transactional
    public Customer createCustomer(CustomerRequest req) {
        // SĐT không bắt buộc. Chỉ kiểm tra trùng khi khách CÓ nhập SĐT
        // (cho phép tạo khách chưa có SĐT, điền/khớp sau khi soát đơn).
        if (req.getPhoneNumber() != null && !req.getPhoneNumber().isBlank()
                && customerRepository.existsByPhone(req.getPhoneNumber())) {
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

        // Kiểm tra trùng SĐT nếu có đổi SĐT (null-safe để tránh NPE khi khách chưa có SĐT)
        if (!java.util.Objects.equals(c.getPhone(), req.getPhoneNumber())
                && req.getPhoneNumber() != null && !req.getPhoneNumber().isBlank()
                && customerRepository.existsByPhone(req.getPhoneNumber())) {
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
                .filter(g -> Boolean.TRUE.equals(g.getAutoUpdate()))
                .collect(Collectors.toList());

        List<String> manualGroupNames = allGroups.stream()
                .filter(g -> !Boolean.TRUE.equals(g.getAutoUpdate()))
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
        // Không có rule -> coi như không khớp (tránh việc nhóm rỗng "ăn" toàn bộ khách)
        if (condObj == null || condObj.getRules() == null || condObj.getRules().isEmpty()) {
            return false;
        }

        List<Boolean> results = new ArrayList<>();

        for (RuleDTO rule : condObj.getRules()) {
            java.math.BigDecimal customerValue = java.math.BigDecimal.ZERO;
            if ("ORDER_COUNT".equals(rule.getField())) {
                customerValue = customer.getOrderCount() != null
                        ? java.math.BigDecimal.valueOf(customer.getOrderCount()) : java.math.BigDecimal.ZERO;
            } else if ("TOTAL_SPENT".equals(rule.getField())) {
                customerValue = customer.getTotalSpent() != null
                        ? customer.getTotalSpent() : java.math.BigDecimal.ZERO;
            }

            java.math.BigDecimal targetValue;
            try {
                targetValue = new java.math.BigDecimal(rule.getValue());
            } catch (NumberFormatException | NullPointerException e) {
                // Giá trị cấu hình không hợp lệ -> rule này không khớp, ghi log để dễ truy vết
                log.warn("Giá trị điều kiện nhóm không hợp lệ: {}", rule.getValue());
                results.add(false);
                continue;
            }

            int cmp = customerValue.compareTo(targetValue);
            boolean match;
            switch (rule.getOperator() != null ? rule.getOperator() : "") {
                case ">":  match = cmp > 0; break;
                case ">=": match = cmp >= 0; break;
                case "<":  match = cmp < 0; break;
                case "<=": match = cmp <= 0; break;
                case "=":  match = cmp == 0; break;
                case "!=": match = cmp != 0; break;
                default:   match = false; break; // toán tử lạ -> không khớp
            }
            results.add(match);
        }

        if ("ALL".equals(condObj.getMatchType())) {
            return !results.contains(false); // ALL: Phải đúng hết
        } else {
            return results.contains(true);   // ANY: Chỉ cần 1 cái đúng
        }
    }

    public Customer getByCode(String orderCode) {
        return customerRepository.findByCode(orderCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng: " + orderCode));

    }
}