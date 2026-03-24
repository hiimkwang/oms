package com.oms.module.maintenance.controller;

import com.oms.module.maintenance.dto.MaintenanceTicketRequest;
import com.oms.module.maintenance.entity.MaintenanceTicket;
import com.oms.module.maintenance.service.MaintenanceTicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/maintenance")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class MaintenanceTicketController {

    private final MaintenanceTicketService maintenanceTicketService;

    @GetMapping
    public ResponseEntity<List<MaintenanceTicket>> getAllTickets() {
        return ResponseEntity.ok(maintenanceTicketService.getAllTickets());
    }

    @GetMapping("/{ticketCode}")
    public ResponseEntity<MaintenanceTicket> getTicketByCode(@PathVariable String ticketCode) {
        return ResponseEntity.ok(maintenanceTicketService.getTicketByCode(ticketCode));
    }

    @PostMapping
    public ResponseEntity<MaintenanceTicket> createTicket(@Valid @RequestBody MaintenanceTicketRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(maintenanceTicketService.createTicket(request));
    }

    // API hỗ trợ cập nhật nhanh trạng thái và chi phí khi sửa xong
    @PatchMapping("/{ticketCode}/status")
    public ResponseEntity<MaintenanceTicket> updateStatus(
            @PathVariable String ticketCode,
            @RequestParam String status,
            @RequestParam(required = false) Double actualCost,
            @RequestParam(required = false) String processingDetails) {
        return ResponseEntity.ok(maintenanceTicketService.updateTicketStatus(ticketCode, status, actualCost, processingDetails));
    }

    @DeleteMapping("/{ticketCode}")
    public ResponseEntity<Void> deleteTicket(@PathVariable String ticketCode) {
        maintenanceTicketService.deleteTicket(ticketCode);
        return ResponseEntity.noContent().build();
    }
}