package com.notification.service.notification_service.service;

import com.notification.service.notification_service.event.EmailVerificationRequestEvent;
import com.notification.service.notification_service.event.OrderPlacedEvent;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender javaMailSender;

    @KafkaListener(topics = "${kafka.topics.order-placed:order-placed}")
    public void orderPlaced(OrderPlacedEvent orderPlacedEvent) {
        log.info("Got message from order-palced topic {}", orderPlacedEvent);
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom("mohab@gmail.com");
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

    @KafkaListener(topics = "${kafka.topics.email-verification:email-verification-topic}")
    public void handleEmailVerificationRequest(@Payload EmailVerificationRequestEvent event) {

        try {
            log.info("=== EMAIL VERIFICATION REQUEST RECEIVED FROM KAFKA ===");
            log.info("User ID: {}", event.getUserId());
            log.info("Email: {}", event.getEmail());
            log.info("Name: {} {}", event.getFirstName(), event.getLastName());
            log.info("Verification Token: {}", event.getVerificationToken());
            log.info("Verification URL: {}", event.getVerificationUrl());

            this.sendEmailVerification(event);

            log.info("Email verification event processed successfully for user: {}", event.getUserId());

        } catch (Exception e) {
            log.error("Failed to process email verification event for user: {}", event.getUserId(), e);
        }
    }

    public void sendEmailVerification(EmailVerificationRequestEvent event) {
        log.info("Sending email verification to: {}", event.getEmail());

        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom("noreply@eshop.com");
            messageHelper.setTo(event.getEmail());
            messageHelper.setSubject("Email Verification - Eshop");
            messageHelper.setText(String.format("""
                    Hi %s %s,

                    Welcome to Eshop! Please verify your email address by clicking the link below:

                    %s

                    If you didn't create this account, please ignore this email.

                    This verification link will expire in 24 hours.

                    Best regards,
                    Eshop Team
                    """,
                    event.getFirstName(),
                    event.getLastName(),
                    event.getVerificationUrl()));
        };

        try {
            javaMailSender.send(messagePreparator);
            log.info("Email verification sent successfully to: {}", event.getEmail());
        } catch (Exception e) {
            log.error("Failed to send email verification to: {}", event.getEmail(), e);
            throw new RuntimeException("Failed to send email verification", e);
        }
    }
}
