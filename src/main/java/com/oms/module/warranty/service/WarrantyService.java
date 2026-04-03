package com.oms.module.warranty.service;

import com.oms.module.account.entity.User;
import com.oms.module.warranty.entity.WarrantyActivity;
import com.oms.module.warranty.entity.WarrantyTicket;
import com.oms.module.warranty.repository.WarrantyTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class WarrantyService {

    private final WarrantyTicketRepository warrantyRepo;

    // --- HÀM TIỆN ÍCH LẤY USER VÀ GHI LOG ---
    private String getCurrentUserName() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof User) {
                return ((User) principal).getFullName();
            }
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "Hệ thống";
        }
    }

    private void logActivity(WarrantyTicket ticket, String action, String description) {
        WarrantyActivity activity = WarrantyActivity.builder()
                .ticket(ticket)
                .action(action)
                .description(description)
                .creatorName(getCurrentUserName())
                .build();
        if (ticket.getActivities() == null) {
            ticket.setActivities(new ArrayList<>());
        }
        ticket.getActivities().add(activity);
    }

    private String getStatusName(WarrantyTicket.TicketStatus status) {
        if (status == null) return "";
        switch (status) {
            case RECEIVED:
                return "Mới tiếp nhận";
            case PROCESSING:
                return "Đang xử lý";
            case DONE:
                return "Đã sửa xong";
            case RETURNED:
                return "Đã trả khách";
            case CANCELED:
                return "Đã hủy";
            default:
                return status.name();
        }
    }
    // ----------------------------------------

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
        if (request.getTicketCode() == null || request.getTicketCode().trim().isEmpty()) {
            request.setTicketCode("BH-" + System.currentTimeMillis());
        }

        request.setStatus(WarrantyTicket.TicketStatus.RECEIVED);

        if (request.getReceiveDate() == null) {
            request.setReceiveDate(LocalDateTime.now());
        }

        // Ghi log tạo mới
        logActivity(request, "Tạo phiếu bảo hành", "Tiếp nhận thiết bị của khách hàng: " + request.getCustomerName());

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

        // 1. KIỂM TRA SỰ THAY ĐỔI ĐỂ GHI LOG
        if (existing.getStatus() != request.getStatus()) {
            logActivity(existing, "Cập nhật trạng thái", "Chuyển từ [" + getStatusName(existing.getStatus()) + "] sang [" + getStatusName(request.getStatus()) + "]");
        }

        String oldDesc = existing.getIssueDescription() != null ? existing.getIssueDescription() : "";
        String newDesc = request.getIssueDescription() != null ? request.getIssueDescription() : "";
        if (!Objects.equals(oldDesc.trim(), newDesc.trim())) {
            logActivity(existing, "Cập nhật Ghi chú / Lỗi", "Ghi chú mới: " + newDesc);
        }

        // 2. CẬP NHẬT DỮ LIỆU
        existing.setCustomerName(request.getCustomerName());
        existing.setCustomerPhone(request.getCustomerPhone());
        existing.setIssueDescription(request.getIssueDescription());
        existing.setRepairCost(request.getRepairCost());
        existing.setReturnDate(request.getReturnDate());
        existing.setBranchId(request.getBranchId());
        existing.setStatus(request.getStatus());
        existing.setType(request.getType());

        return warrantyRepo.save(existing);
    }
}