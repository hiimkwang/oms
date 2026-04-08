package com.oms.module.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConditionDTO {
    private String matchType;
    private List<RuleDTO> rules;
}