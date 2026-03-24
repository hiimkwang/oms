package com.oms.module.quotation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class QuotationRequest {
    @NotBlank(message = "Số báo giá không được để trống")
    private String quotationCode;

    @NotBlank(message = "Mã khách hàng không được để trống")
    private String customerCode;

    private LocalDate quotationDate;
    private LocalDate validUntil;
    private String staffName;
    private Double taxPercent = 0.0;
    private String status = "Chờ duyệt";
    private String note;

    @NotEmpty(message = "Báo giá phải có ít nhất 1 sản phẩm")
    @Valid
    private List<QuotationDetailRequest> details;
}