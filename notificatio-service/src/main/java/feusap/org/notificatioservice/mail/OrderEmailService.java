package feusap.org.notificatioservice.mail;

import feusap.org.notificatioservice.dto.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Sends the order confirmation email for an {@link OrderEvent}.
 */
@Slf4j
@Service
public class OrderEmailService {

    private final JavaMailSender mailSender;
    private final String from;

    public OrderEmailService(JavaMailSender mailSender,
                             @Value("${notification.mail.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    /**
     * @return true when the email was handed to the SMTP server, false when the event has no recipient
     * @throws MailException when the SMTP server rejects or cannot be reached (the listener can retry)
     */
    public boolean sendOrderConfirmation(OrderEvent event) {
        if (event.customerEmail() == null || event.customerEmail().isBlank()) {
            log.warn("Order {} has no customer email, confirmation skipped", event.orderId());
            return false;
        }
        mailSender.send(buildConfirmation(event));
        log.info("Confirmation email sent for order {} to {}", event.orderId(), event.customerEmail());
        return true;
    }

    SimpleMailMessage buildConfirmation(OrderEvent event) {
        var message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(event.customerEmail());
        message.setSubject("Confirmation de votre commande " + event.orderId());
        message.setText("""
                Bonjour %s,

                Nous avons bien reçu votre commande.

                Commande : %s
                Produit  : %s
                Quantité : %d
                Total    : %s $

                Merci de votre confiance.
                """.formatted(
                event.customerId(),
                event.orderId(),
                event.productName(),
                event.quantity(),
                event.totalAmount()
        ));
        return message;
    }
}
