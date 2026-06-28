package com.oms.module.reconciliation.dto;

import lombok.*;

import java.util.List;

/** Kết quả sau khi đồng bộ về OMS. */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReconcileSyncResult {
    private boolean success;
    private int createdCount;   // số đơn bù đã tạo
    private int filledCount;    // số đơn được điền mã đơn sàn/vận đơn
    private int paidCount;      // số đơn được đánh dấu đã thanh toán
    private List<String> errors;
}
