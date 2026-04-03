package com.oms.module.returnorder.controller;

import com.oms.module.returnorder.dto.ReturnOrderRequest;
import com.oms.module.returnorder.entity.ReturnOrder;
import com.oms.module.returnorder.service.ReturnOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/returns")
@RequiredArgsConstructor
public class ReturnOrderApiController {

    private final ReturnOrderService returnService;

    @PostMapping
    public ResponseEntity<ReturnOrder> createReturnOrder(@RequestBody ReturnOrderRequest request) {
        return ResponseEntity.ok(returnService.createReturnOrder(request));
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<?> processRefund(@PathVariable Long id, @RequestParam String method) {
        returnService.processRefund(id, method);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/restock")
    public ResponseEntity<?> processRestock(@PathVariable Long id, @RequestParam Long branchId) {
        returnService.processRestock(id, branchId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/bulk")
    public ResponseEntity<?> deleteBulk(@RequestBody List<Long> ids) {
        try {
            returnService.deleteBulk(ids);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}