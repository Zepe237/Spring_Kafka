package feusap.org.notificatioservice.consumer;
import feusap.org.notificatioservice.dto.OrderEvent;
import feusap.org.notificatioservice.mail.OrderEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationConsumer {

    private final OrderEmailService orderEmailService;

    @KafkaListener(
            topics = "${kafka.topic.orders}",
            groupId = "notification-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumerOrderEvent(
            ConsumerRecord<String, OrderEvent> consumerRecord,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        var event = consumerRecord.value();
        log.info("""
                New message received:
                Topic: {}
                Partition: {}
                Offset: {}
                Key: {}
                OrderId: {}
                Customer: {} <{}>
                Product: {}
                Amount: {}
                Status: {}
                """, consumerRecord.topic(), partition, offset, consumerRecord.key(), event.orderId(), event.customerId(), event.customerEmail(), event.productName(), event.totalAmount(), event.status());
        processOrderEvent(event);

    }

    private void processOrderEvent(OrderEvent event) {
        log.info("Processing order event for order ID: {}", event.orderId());
        orderEmailService.sendOrderConfirmation(event);
    }

}
