package com.feusap.order_service.controller;

import com.feusap.order_service.dto.CreateOrderRequest;
import com.feusap.order_service.dto.OrderEvent;
import com.feusap.order_service.producer.OrderProducer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

import static org.springframework.http.HttpStatus.ACCEPTED;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class orderController {
    private final OrderProducer orderProducer;
    @Operation(summary = "Create Order Event", description = "create an OrderEvent")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Event created",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = OrderEvent.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = OrderEvent.class)
                            )
                    ),
            }
    )
    @PostMapping
    public ResponseEntity<Map<String, String>> placeOrder(
            @Valid @RequestBody CreateOrderRequest request
            ){
        var orderId = UUID.randomUUID().toString();
        var event = OrderEvent.of(
                orderId,
                request.customerId(),
                request.productName(),
                request.quantity(),
                request.totalAmount()
        );
        orderProducer.sendOrder(event);
        log.info("Order placed succesfully: {}", event.orderId());

        return ResponseEntity
                .status(ACCEPTED)
                .body(Map.of(
                        "orderId", orderId,
                        "status", "CREATED",
                        "message", "Order placed Succesfully and under processing"
                ));
    }
}
