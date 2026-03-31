package com.oms.module.setting.controller;

import com.oms.module.account.repository.UserRepository;
import com.oms.module.setting.repository.BranchRepository;
import com.oms.module.setting.repository.SalesChannelRepository;
import com.oms.module.setting.service.MasterDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/ui/settings")
@RequiredArgsConstructor
public class SettingController {

    private final BranchRepository branchRepository;
    private final SalesChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final MasterDataService masterDataService;

    // 1. Cấu hình chung
    @GetMapping("/general")
    public String general(Model model) {
        model.addAttribute("activeMenu", "general");

        // Lấy các cấu hình cũ (nếu có) truyền xuống View
        List<String> configKeys = java.util.Arrays.asList("STORE_NAME", "STORE_PHONE", "STORE_ADDRESS", "STORE_LOGO", "STORE_EMAIL");
        model.addAttribute("configs", masterDataService.getSystemConfigMap(configKeys));

        return "settings/general";
    }

    // 2. Quản lý chi nhánh
    @GetMapping("/branches")
    public String branches(Model model) {
        model.addAttribute("branches", branchRepository.findAll());
        model.addAttribute("activeMenu", "branches");
        return "settings/branches";
    }

    // 3. Quản lý nhân viên (Admin tạo tài khoản)
    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("branches", branchRepository.findAll()); // Để chọn chi nhánh khi tạo NV
        model.addAttribute("activeMenu", "users");
        return "settings/users";
    }

    // 4. Kênh bán hàng
    @GetMapping("/channels")
    public String channels(Model model) {
        model.addAttribute("channels", channelRepository.findAll());
        model.addAttribute("activeMenu", "channels");
        return "settings/channels";
    }
}