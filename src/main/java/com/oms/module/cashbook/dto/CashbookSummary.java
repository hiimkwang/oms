package com.oms.module.cashbook.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CashbookSummary {
    // 1. Quỹ đầu kỳ (Tiền tồn trước khoảng thời gian lọc)
    private BigDecimal openingBalance;

    // 2. Tổng thu trong kỳ (Tổng các phiếu PT trong khoảng lọc)
    private BigDecimal totalIn;

    // 3. Tổng chi trong kỳ (Tổng các phiếu PC trong khoảng lọc)
    private BigDecimal totalOut;

    // 4. Tồn quỹ ( = openingBalance + totalIn - totalOut)
    private BigDecimal closingBalance;

    // 5. Số dư tiền mặt hiện tại (Tổng thu CASH - Tổng chi CASH)
    private BigDecimal cashBalance;

    // 6. Số dư tiền gửi hiện tại (Tổng thu BANK - Tổng chi BANK)
    private BigDecimal bankBalance;
}