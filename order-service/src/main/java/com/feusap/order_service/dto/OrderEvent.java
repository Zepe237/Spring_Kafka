package com.feusap.order_service.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderEvent (
        String orderId,
        String customerId,
        String customerEmail,
        String productName,
        int quantity,
        BigDecimal totalAmount,
        String status,
        Instant createAt
) {
    public static OrderEvent of(
            String orderId,
            String customerId,
            String customerEmail,
            String productName,
            int quantity,
            BigDecimal totalAmount
    ) {
        return new OrderEvent(
                orderId,
                customerId,
                customerEmail,
                productName,
                quantity,
                totalAmount,
                "CREATED",
                Instant.now()
        );
    }
}
