package com.oms.module.customer.dto; // Đổi package theo project của bạn nhé

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConditionDTO {
    // Tương ứng với biến matchType trong JSON ("ALL" hoặc "ANY")
    private String matchType;

    // Tương ứng với mảng rules trong JSON
    private List<RuleDTO> rules;
}