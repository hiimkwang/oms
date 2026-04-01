package com.oms.module.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TargetDropdownResponse {
    private Long id;
    private String name;
    private String code;
}
