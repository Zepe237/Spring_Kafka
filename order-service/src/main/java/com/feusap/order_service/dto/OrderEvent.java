package com.feusap.order_service.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderEvent (
        String orderId ,
        String customerId,
        String productName,
        int quantity,
        BigDecimal totalAmount,
        String status,
        Instant createAt
) {
    public static @NotNull OrderEvent of(
            String orderId,
            String customerId,
            String productName,
            int quantity,
            BigDecimal totalAmount

            ){
        return new OrderEvent(
                orderId,
                customerId,
                productName,
                quantity,
                totalAmount,
                "CREATED",
                Instant.now()
        );
    }
}
