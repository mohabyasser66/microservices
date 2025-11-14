package com.notification.service.notification_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationRequestEvent {
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private String verificationToken;
    private String verificationUrl;
}