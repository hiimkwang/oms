package com.oms.module.product.dto;

import lombok.Data;
import java.util.List;

@Data
public class ProductAttributeRequest {
    private String name;
    private List<String> values;
}