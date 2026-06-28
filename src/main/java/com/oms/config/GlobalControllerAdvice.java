package com.oms.config;

import com.oms.module.account.entity.User;
import com.oms.module.account.repository.UserRepository;
import com.oms.module.setting.entity.MasterData;
import com.oms.module.setting.repository.MasterDataRepository;
import org.springframework.boot.json.JsonParseException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import tools.jackson.databind.ObjectMapper;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final UserRepository userRepository;
    private final MasterDataRepository masterDataRepository;
    private final ObjectMapper objectMapper;

    public GlobalControllerAdvice(UserRepository userRepository, MasterDataRepository masterDataRepository, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.masterDataRepository = masterDataRepository;
        this.objectMapper = objectMapper;
    }

    @ModelAttribute("currentUser")
    public User getCurrentUser(Principal principal) {
        if (principal != null) {
            return userRepository.findByUsername(principal.getName()).orElse(null);
        }
        return null;
    }

    // Cờ quyền ADMIN dùng chung cho mọi template (ẩn giá vốn/lãi lỗ/vốn với nhân viên)
    @ModelAttribute("isAdmin")
    public boolean isAdmin() {
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    @ModelAttribute("storeName")
    public String getStoreName() {
        List<MasterData> dataList = masterDataRepository.findByDataTypeOrderBySortOrderAsc("STORE_NAME");
        if (!dataList.isEmpty() && dataList.get(0).getDataValue() != null) {
            return dataList.get(0).getDataValue();
        }
        return "OMS";
    }

    @ModelAttribute("storeLogo")
    public String getStoreLogo() {
        List<MasterData> dataList = masterDataRepository.findByDataTypeOrderBySortOrderAsc("STORE_LOGO");
        if (!dataList.isEmpty() && dataList.get(0).getDataValue() != null) {
            return dataList.get(0).getDataValue();
        }
        return null;
    }
    private Map<String, String> getMapByType(String dataType) {
        Map<String, String> map = new HashMap<>();
        List<MasterData> dataList = masterDataRepository.findByDataTypeOrderBySortOrderAsc(dataType);
        dataList.forEach(d -> map.put(d.getDataValue(), d.getDataLabel() != null ? d.getDataLabel() : d.getDataValue()));
        return map;
    }

    private String getJsonByType(String dataType) {
        try {
            return objectMapper.writeValueAsString(getMapByType(dataType));
        } catch (JsonParseException e) {
            return "{}";
        }
    }

    // --- 1. DÀNH CHO ORDER (ĐƠN HÀNG) ---
    @ModelAttribute("orderStatuses")
    public List<MasterData> getOrderStatuses() {
        return masterDataRepository.findByDataTypeOrderBySortOrderAsc("ORDER_STATUS");
    }

    @ModelAttribute("orderStatusMap")
    public Map<String, String> getOrderStatusMap() {
        return getMapByType("ORDER_STATUS");
    }

    @ModelAttribute("orderStatusMapJson")
    public String getOrderStatusMapJson() {
        return getJsonByType("ORDER_STATUS");
    }

    // --- 2. DÀNH CHO RETURN (TRẢ HÀNG) ---
    @ModelAttribute("returnStatusMap")
    public Map<String, String> getReturnStatusMap() {
        return getMapByType("RETURN_STATUS");
    }

    @ModelAttribute("returnStatusMapJson")
    public String getReturnStatusMapJson() {
        return getJsonByType("RETURN_STATUS");
    }

    // --- 3. DÀNH CHO THANH TOÁN ---
    @ModelAttribute("paymentStatusMap")
    public Map<String, String> getPaymentStatusMap() {
        return getMapByType("PAYMENT_STATUS");
    }

    @ModelAttribute("paymentStatusMapJson")
    public String getPaymentStatusMapJson() {
        return getJsonByType("PAYMENT_STATUS");
    }

    // --- 4. DÀNH CHO NHẬP HÀNG (RECEIPT) ---
    @ModelAttribute("receiptStatusMap")
    public Map<String, String> getReceiptStatusMap() {
        return getMapByType("RECEIPT_STATUS");
    }

    @ModelAttribute("receiptStatusMapJson")
    public String getReceiptStatusMapJson() {
        return getJsonByType("RECEIPT_STATUS");
    }
}