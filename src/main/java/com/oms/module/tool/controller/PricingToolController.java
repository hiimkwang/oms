package com.oms.module.tool.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ui/tools")
public class PricingToolController {

    // 1. Màn hình tính giá nhập kho nháp (Bạn đổi tên file html cũ thành import-pricing.html)
    @GetMapping("/import-pricing")
    public String importPricingCalculator(Model model) {
        return "tools/import-pricing";
    }

    // 2. Màn hình phân tích Giá bán & Lợi nhuận sàn TMĐT (File mới)
    @GetMapping("/selling-pricing")
    public String sellingPricingCalculator(Model model) {
        return "tools/selling-pricing";
    }
}