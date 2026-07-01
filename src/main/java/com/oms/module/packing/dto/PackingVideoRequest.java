package com.oms.module.packing.dto;

import lombok.Data;

/** Gắn đường dẫn video đóng gói (trên máy client) vào đơn. */
@Data
public class PackingVideoRequest {
    private String videoPath;
}
