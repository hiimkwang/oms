package com.oms.module.maintenance.service;

import com.oms.module.customer.entity.Customer;
import com.oms.module.customer.service.CustomerService;
import com.oms.module.maintenance.dto.MaintenanceTicketRequest;
import com.oms.module.maintenance.entity.MaintenanceTicket;
import com.oms.module.maintenance.repository.MaintenanceTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MaintenanceTicketService {

    private final MaintenanceTicketRepository maintenanceTicketRepository;
    private final CustomerService customerService;

    public List<MaintenanceTicket> getAllTickets() {
        return maintenanceTicketRepository.findAll();
    }

    public MaintenanceTicket getTicketByCode(String ticketCode) {
        return maintenanceTicketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhận thiết bị: " + ticketCode));
    }

    public MaintenanceTicket createTicket(MaintenanceTicketRequest request) {
        if (maintenanceTicketRepository.existsByTicketCode(request.getTicketCode())) {
            throw new RuntimeException("Số phiếu đã tồn tại: " + request.getTicketCode());
        }

        Customer customer = customerService.getCustomerByCode(request.getCustomerCode());

        MaintenanceTicket ticket = MaintenanceTicket.builder()
                .ticketCode(request.getTicketCode())
                .receiveDate(request.getReceiveDate())
                .customer(customer)
                .productName(request.getProductName())
                .serialNumber(request.getSerialNumber())
                .reportedDefect(request.getReportedDefect())
                .actualCondition(request.getActualCondition())
                .estimatedCost(request.getEstimatedCost())
                .customerAgreed(request.getCustomerAgreed())
                .technician(request.getTechnician())
                .processingDetails(request.getProcessingDetails())
                .actualCost(request.getActualCost())
                .returnDate(request.getReturnDate())
                .status(request.getStatus())
                .note(request.getNote())
                .build();

        return maintenanceTicketRepository.save(ticket);
    }

    public MaintenanceTicket updateTicketStatus(String ticketCode, String status, Double actualCost, String processingDetails) {
        MaintenanceTicket ticket = getTicketByCode(ticketCode);

        if (status != null) ticket.setStatus(status);
        if (actualCost != null) ticket.setActualCost(actualCost);
        if (processingDetails != null) ticket.setProcessingDetails(processingDetails);

        return maintenanceTicketRepository.save(ticket);
    }

    public void deleteTicket(String ticketCode) {
        MaintenanceTicket ticket = getTicketByCode(ticketCode);
        maintenanceTicketRepository.delete(ticket);
    }
}