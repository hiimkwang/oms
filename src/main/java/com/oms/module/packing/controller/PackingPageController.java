package com.oms.module.packing.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PackingPageController {

    /** Trang trạm đóng gói & quay video. */
    @GetMapping("/ui/packing")
    public String packingPage() {
        return "packing/packing";
    }
}
