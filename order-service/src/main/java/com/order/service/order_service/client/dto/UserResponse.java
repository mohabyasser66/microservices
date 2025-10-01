package com.order.service.order_service.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User response DTO for communication with User Service
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    /**
     * Nested address response for user addresses
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AddressResponse {
        private UUID id;
        private String firstName;
        private String lastName;
        private String company;
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String postalCode;
        private String country;
        private String phoneNumber;
        private String email;
        private boolean isDefault;
        private String addressType; // SHIPPING, BILLING
    }
}
