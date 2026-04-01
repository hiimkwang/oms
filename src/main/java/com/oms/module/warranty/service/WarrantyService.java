package com.oms.module.warranty.service;

import com.oms.module.warranty.entity.WarrantyTicket;
import com.oms.module.warranty.repository.WarrantyTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WarrantyService {

    private final WarrantyTicketRepository warrantyRepo;

    public List<WarrantyTicket> filterTickets(String keyword, String statusStr, String typeStr) {
        WarrantyTicket.TicketStatus status = null;
        WarrantyTicket.TicketType type = null;

        try {
            if (statusStr != null && !statusStr.isEmpty())
                status = WarrantyTicket.TicketStatus.valueOf(statusStr.toUpperCase());
        } catch (Exception e) {
        }
        try {
            if (typeStr != null && !typeStr.isEmpty()) type = WarrantyTicket.TicketType.valueOf(typeStr.toUpperCase());
        } catch (Exception e) {
        }

        return warrantyRepo.filterTickets(keyword, status, type);
    }

    @Transactional
    public WarrantyTicket createTicket(WarrantyTicket request) {
        // Tự sinh mã nếu rỗng
        if (request.getTicketCode() == null || request.getTicketCode().trim().isEmpty()) {
            request.setTicketCode("BH-" + System.currentTimeMillis()); // Hoặc viết logic sinh mã tuần tự của anh
        }

        request.setStatus(WarrantyTicket.TicketStatus.RECEIVED); // Mặc định là Mới tiếp nhận

        if (request.getReceiveDate() == null) {
            request.setReceiveDate(LocalDateTime.now());
        }

        return warrantyRepo.save(request);
    }

    @Transactional
    public void deleteBulk(List<Long> ids) {
        warrantyRepo.deleteAllById(ids);
    }

    public WarrantyTicket getById(Long id) {
        return warrantyRepo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu bảo hành!"));
    }

    @Transactional
    public WarrantyTicket updateTicket(Long id, WarrantyTicket request) {
        WarrantyTicket existing = getById(id);

        // Cập nhật các trường được phép sửa
        existing.setCustomerName(request.getCustomerName());
        existing.setCustomerPhone(request.getCustomerPhone());
        existing.setIssueDescription(request.getIssueDescription());
        existing.setRepairCost(request.getRepairCost());
        existing.setReturnDate(request.getReturnDate());
        existing.setBranchId(request.getBranchId());

        // Quan trọng: Cập nhật Trạng thái và Loại dịch vụ
        existing.setStatus(request.getStatus());
        existing.setType(request.getType());

        return warrantyRepo.save(existing);
    }
}