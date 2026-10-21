package feusap.org.notificatioservice;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.kafka.KafkaContainer;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

/**
 * End-to-end inside the service: a JSON order event published to a real Kafka broker
 * (Testcontainers) is consumed by the listener and turns into a confirmation email.
 * The SMTP sender is mocked so the test checks the email without a mail server.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class OrderNotificationIntegrationTest {

    @Autowired
    private KafkaContainer kafka;

    @MockitoBean
    private JavaMailSender mailSender;

    @Value("${kafka.topic.orders}")
    private String ordersTopic;

    @Test
    void orderEventFromKafkaSendsConfirmationEmail() throws Exception {
        String json = """
                {"orderId":"order-it-2","customerId":"Client-007","customerEmail":"client@example.com",
                 "productName":"Pack TCF Gold","quantity":1,"totalAmount":35.00,
                 "status":"CREATED","createAt":"2026-10-20T12:00:00Z"}
                """;

        try (var producer = new KafkaProducer<String, String>(Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers()
        ), new StringSerializer(), new StringSerializer())) {
            producer.send(new ProducerRecord<>(ordersTopic, "order-it-2", json)).get(10, TimeUnit.SECONDS);
        }

        var captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, timeout(30_000)).send(captor.capture());

        SimpleMailMessage message = captor.getValue();
        assertThat(message.getTo()).containsExactly("client@example.com");
        assertThat(message.getSubject()).contains("order-it-2");
    }
}
