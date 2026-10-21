package com.feusap.order_service;

import com.feusap.order_service.dto.OrderEvent;
import com.feusap.order_service.producer.OrderProducer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.kafka.KafkaContainer;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Publishes an order through the real producer and reads it back from a real
 * Kafka broker started by Testcontainers.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class OrderProducerIntegrationTest {

    @Autowired
    private OrderProducer orderProducer;

    @Autowired
    private KafkaContainer kafka;

    @Value("${kafka.topics.orders}")
    private String ordersTopic;

    @Test
    void publishedOrderIsReadableFromTopicAsJson() throws Exception {
        var event = OrderEvent.of("order-it-1", "customer-42", "client@example.com", "Pack TCF Gold", 2, new BigDecimal("70.00"));

        var result = orderProducer.sendOrder(event).get(10, TimeUnit.SECONDS);
        assertThat(result.getRecordMetadata().topic()).isEqualTo(ordersTopic);

        try (var consumer = new KafkaConsumer<String, String>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "it-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"
        ), new StringDeserializer(), new StringDeserializer())) {
            consumer.subscribe(List.of(ordersTopic));

            String payload = null;
            long deadline = System.currentTimeMillis() + 15_000;
            while (payload == null && System.currentTimeMillis() < deadline) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, String> record : records) {
                    if (record.value().contains("order-it-1")) {
                        payload = record.value();
                    }
                }
            }

            assertThat(payload).as("order event received from Kafka").isNotNull();
            assertThat(payload)
                    .contains("\"customerId\":\"customer-42\"")
                    .contains("\"customerEmail\":\"client@example.com\"")
                    .contains("\"productName\":\"Pack TCF Gold\"")
                    .contains("\"status\":\"CREATED\"");
        }
    }
}
