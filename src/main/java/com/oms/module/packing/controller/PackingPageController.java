package com.oms.module.packing.controller;

import com.oms.module.setting.service.BranchService;
import com.oms.module.setting.service.SalesChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class PackingPageController {

    private final SalesChannelService salesChannelService;
    private final BranchService branchService;

    /** Trang trạm đóng gói & quay video. */
    @GetMapping("/ui/packing")
    public String packingPage(Model model) {
        model.addAttribute("channels", salesChannelService.findAll());
        model.addAttribute("branches", branchService.findAll());
        return "packing/packing";
    }
}
