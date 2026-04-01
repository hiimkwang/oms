package com.oms.config;

import com.oms.module.account.entity.User;
import com.oms.module.account.repository.UserRepository;
import com.oms.module.setting.entity.MasterData;
import com.oms.module.setting.repository.MasterDataRepository;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;
import java.util.List;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final UserRepository userRepository;
    private final MasterDataRepository masterDataRepository;

    public GlobalControllerAdvice(UserRepository userRepository, MasterDataRepository masterDataRepository) {
        this.userRepository = userRepository;
        this.masterDataRepository = masterDataRepository;
    }

    @ModelAttribute("currentUser")
    public User getCurrentUser(Principal principal) {
        if (principal != null) {
            return userRepository.findByUsername(principal.getName()).orElse(null);
        }
        return null;
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
}