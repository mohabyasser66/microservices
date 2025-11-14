package com.user.service.users_service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventPublisher {

    private final KafkaTemplate<String, EmailVerificationRequestEvent> emailVerificationKafkaTemplate;

    @Value("${kafka.topics.email-verification:email-verification-topic}")
    private String emailVerificationTopic;

    public void publishEmailVerificationRequest(EmailVerificationRequestEvent event) {
        try {
            emailVerificationKafkaTemplate.send(emailVerificationTopic, event);
            log.info("Email verification event published to Kafka for user: {} on topic: {}",
                    event.getUserId(), emailVerificationTopic);
        } catch (Exception e) {
            log.error("Failed to publish email verification event to Kafka for user: {}", event.getUserId(), e);
            throw new RuntimeException("Failed to publish email verification event", e);
        }
    }
}