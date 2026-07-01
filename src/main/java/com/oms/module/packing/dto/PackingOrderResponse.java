package com.oms.module.packing.dto;

import com.oms.module.order.entity.Order;
import com.oms.module.order.entity.OrderDetail;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Phản hồi an toàn cho trạm đóng gói: KHÔNG lộ giá vốn (costPrice).
 */
@Data
@Builder
public class PackingOrderResponse {
    private String orderCode;
    private String trackingCode;
    private String status;
    private String customerName;
    private String shippingAddress;
    private String note;
    private BigDecimal totalAmount;
    private String packingVideoPath;
    private List<Item> items;

    @Data
    @Builder
    public static class Item {
        private Long id;
        private String sku;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private String serialNumber;
    }

    public static PackingOrderResponse from(Order order) {
        List<Item> items = new ArrayList<>();
        if (order.getDetails() != null) {
            for (OrderDetail d : order.getDetails()) {
                items.add(Item.builder()
                        .id(d.getId())
                        .sku(d.getSku())
                        .productName(d.getProductName())
                        .quantity(d.getQuantity())
                        .unitPrice(d.getUnitPrice())
                        .serialNumber(d.getSerialNumber())
                        .build());
            }
        }
        return PackingOrderResponse.builder()
                .orderCode(order.getOrderCode())
                .trackingCode(order.getTrackingCode())
                .status(order.getStatus())
                .customerName(order.getCustomer() != null ? order.getCustomer().getFullName() : null)
                .shippingAddress(order.getShippingAddress())
                .note(order.getNote())
                .totalAmount(order.getTotalAmount())
                .packingVideoPath(order.getPackingVideoPath())
                .items(items)
                .build();
    }
}
