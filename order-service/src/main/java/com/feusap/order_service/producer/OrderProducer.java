package com.feusap.order_service.producer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.feusap.order_service.dto.OrderEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderProducer {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @Value("${kafka.topics.orders}")
    private String ordersTopic;

    public CompletableFuture<SendResult<String, OrderEvent>> sendOrder(OrderEvent event) {
        log.info("Sending order: {} to topic: {}", event, ordersTopic);

        var future = kafkaTemplate.send(ordersTopic, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Error sending order: {}", event.orderId(), ex);
            } else {
                var metadata = result.getRecordMetadata();
                log.info("Order sent successfully: {} to partition: {} at offset: {}", event.orderId(), metadata.partition(), metadata.offset());
            }
        });

        return future;
    }
}