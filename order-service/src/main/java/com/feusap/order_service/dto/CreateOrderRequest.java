package com.feusap.order_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateOrderRequest (
        @NotBlank(message = "Customer ID cannot be blank")
        String customerId,
        @NotBlank(message = "Customer email cannot be blank")
        @Email(message = "Customer email must be a valid address")
        String customerEmail,
        @NotBlank(message = "product name cannot be blank")
        String productName,
        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity,
        @NotNull(message = "Total amount cannot be Blank")
        @DecimalMin(value = "0.01", message = "the total amount must be at least 0.01")
        BigDecimal totalAmount
){
}
