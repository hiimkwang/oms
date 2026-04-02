package com.oms.module.inventory.controller;

import com.oms.module.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryApiController {

    private final InventoryRepository inventoryRepository;

    @GetMapping("/check")
    public ResponseEntity<?> checkStock(
            @RequestParam Long branchId,
            @RequestParam String sku) {

        Integer stock = inventoryRepository.getStockByBranchAndSku(branchId, sku);

        Map<String, Integer> response = new HashMap<>();
        response.put("stock", stock != null ? stock : 0);
        return ResponseEntity.ok(response);
    }
}