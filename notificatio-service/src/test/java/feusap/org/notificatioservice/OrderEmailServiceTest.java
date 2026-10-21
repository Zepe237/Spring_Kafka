package feusap.org.notificatioservice;

import feusap.org.notificatioservice.dto.OrderEvent;
import feusap.org.notificatioservice.mail.OrderEmailService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class OrderEmailServiceTest {

    private final JavaMailSender mailSender = mock(JavaMailSender.class);
    private final OrderEmailService service = new OrderEmailService(mailSender, "commandes@feusap.local");

    private static OrderEvent event(String email) {
        return new OrderEvent("order-1", "Client-001", email, "Pack TCF Gold", 2,
                new BigDecimal("70.00"), "CREATED", Instant.parse("2026-10-20T12:00:00Z"));
    }

    @Test
    void sendsConfirmationToCustomer() {
        boolean sent = service.sendOrderConfirmation(event("client@example.com"));

        var captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage message = captor.getValue();

        assertThat(sent).isTrue();
        assertThat(message.getTo()).containsExactly("client@example.com");
        assertThat(message.getFrom()).isEqualTo("commandes@feusap.local");
        assertThat(message.getSubject()).contains("order-1");
        assertThat(message.getText()).contains("Pack TCF Gold").contains("70.00");
    }

    @Test
    void skipsEventWithoutEmail() {
        boolean sent = service.sendOrderConfirmation(event(" "));

        assertThat(sent).isFalse();
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }
}
