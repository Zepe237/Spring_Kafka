package feusap.org.notificatioservice.dto;

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
}
