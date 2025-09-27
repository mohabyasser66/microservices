package com.user.service.users_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final Map<String, UUID> verificationTokens = new ConcurrentHashMap<>();

    public void sendVerificationEmail(UUID userId, String email) {
        String token = UUID.randomUUID().toString();
        verificationTokens.put(token, userId);

        // (SendGrid, AWS SES, etc.)
        log.info("Sending verification email to {} with token: {}", email, token);
        log.info("Verification URL: http://localhost:8080/api/users/verify-email?token={}", token);
    }

    public UUID getUserIdFromToken(String token) {
        return verificationTokens.get(token);
    }

    public void removeToken(String token) {
        verificationTokens.remove(token);
    }
}
