package com.notification.service.notification_service.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender javaMailSender;

    @KafkaListener(topics = "order-placed", groupId = "notification-group")
    public void listen(com.techie.microservices.order.event.OrderPlacedEvent orderPlacedEvent) {
        log.info("Got message from order-palced topic {}", orderPlacedEvent);
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom("user@gmail.com");
            messageHelper.setTo(orderPlacedEvent.getEmail().toString());
            messageHelper.setSubject(String.format("Order Placed with number %s", orderPlacedEvent.getOrderNumber()));
            messageHelper.setText(String.format("""
                    Hi %s, %s
                    Your order has been placed successfully.
                    Order Number: %s
                    """,
                    orderPlacedEvent.getFirstName().toString(),
                    orderPlacedEvent.getLastName().toString(),
                    orderPlacedEvent.getOrderNumber()));
        };
        try {
            javaMailSender.send(messagePreparator);
        } catch (Exception e) {
            log.error("Failed to send email", e);
            throw new RuntimeException(e);
        }
    }
}


