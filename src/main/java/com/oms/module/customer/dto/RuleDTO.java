package com.oms.module.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RuleDTO {
    private String field;    // Ví dụ: "TOTAL_SPENT" hoặc "ORDER_COUNT"
    private String operator; // Ví dụ: ">", "<", "="
    private String value;    // Ví dụ: "20000000"
}