package com.order.service.order_service.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Tracking response DTO from shipping service
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrackingResponse {
    private String trackingNumber;
    private String carrier;
    private String status; // IN_TRANSIT, DELIVERED, EXCEPTION, PENDING
    private String statusDescription;
    private LocalDateTime lastUpdated;
    private LocalDateTime estimatedDelivery;
    private String message;
    private List<TrackingEvent> events;

    /**
     * Individual tracking event
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TrackingEvent {
        private LocalDateTime timestamp;
        private String status;
        private String description;
        private String location;
        private String city;
        private String state;
        private String country;
    }
}
