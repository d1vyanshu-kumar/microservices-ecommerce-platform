package com.DivyanshuLearn.microservices.notification.service;

import com.DivyanshuLearn.microservices.order.event.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private static final String ORDER_PLACED_TOPIC = "order-placed";
    private static final String FROM_ADDRESS = "springshop@email.com";

    private final JavaMailSender javaMailSender;

    @KafkaListener(topics = ORDER_PLACED_TOPIC)
    public void listen(OrderPlacedEvent orderPlacedEvent) {
        log.info("Received OrderPlacedEvent from topic '{}': {}", ORDER_PLACED_TOPIC, orderPlacedEvent);

        MimeMessagePreparator messagePreparator = buildOrderConfirmationEmail(orderPlacedEvent);

        try {
            javaMailSender.send(messagePreparator);
            log.info("Order confirmation email sent for order {}",
                    orderPlacedEvent.getOrderNumber());
        } catch (MailException ex) {
            log.error("Failed to send email for order {}: {}",
                    orderPlacedEvent.getOrderNumber(), ex.getMessage(), ex);
            // Do not rethrow — avoids infinite retry loops on permanent mail failures.
            // In production, push to a dead-letter topic or alert channel instead.
        }
    }

    private MimeMessagePreparator buildOrderConfirmationEmail(OrderPlacedEvent event) {
        return mimeMessage -> {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
            helper.setFrom(FROM_ADDRESS);
            helper.setTo(event.getEmail().toString());
            helper.setSubject(String.format(
                    "Your Order %s is placed successfully", event.getOrderNumber()));
            helper.setText(String.format("""
                    Hi %s %s,

                    Your order with order number %s has been placed successfully.

                    Best Regards,
                    Spring Shop
                    """,
                    event.getFirstName().toString(),
                    event.getLastName().toString(),
                    event.getOrderNumber()));
        };
    }
}
