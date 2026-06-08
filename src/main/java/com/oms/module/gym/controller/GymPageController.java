package com.oms.module.gym.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Trang gym độc lập, có URL riêng và KHÔNG xuất hiện trong menu OMS.
 * Trang dùng template riêng (không dùng layout/sidebar của OMS).
 */
@Controller
public class GymPageController {

    @GetMapping("/gym")
    public String gymPage() {
        return "gym/gym";
    }
}
