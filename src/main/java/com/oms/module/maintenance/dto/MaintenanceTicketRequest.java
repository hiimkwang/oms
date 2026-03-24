package com.oms.module.maintenance.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MaintenanceTicketRequest {
    @NotBlank(message = "Số phiếu không được để trống")
    private String ticketCode;

    private LocalDate receiveDate;

    @NotBlank(message = "Mã khách hàng không được để trống")
    private String customerCode;

    @NotBlank(message = "Tên thiết bị không được để trống")
    private String productName;

    private String serialNumber;
    private String reportedDefect;
    private String actualCondition;
    private Double estimatedCost = 0.0;
    private Boolean customerAgreed = false;
    private String technician;
    private String processingDetails;
    private Double actualCost = 0.0;
    private LocalDate returnDate;
    private String status = "Tiếp nhận";
    private String note;
}