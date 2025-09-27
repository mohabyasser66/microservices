package com.user.service.users_service.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class LoginHistoryDto {
    private UUID userId;
    private LocalDateTime loginTime;
    private String ipAddress;
    private String userAgent;
    private boolean successful;
}
