package com.order.service.order_service.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Shipping request DTO for shipment creation
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShippingRequest {
    private UUID orderId;
    private String orderNumber;

    // Shipping method
    private String shippingMethod; // STANDARD, EXPRESS, OVERNIGHT

    // Package details
    private BigDecimal weight;
    private String weightUnit;
    private String packageType;

    // Shipping address
    private String recipientName;
    private String recipientPhone;
    private String recipientEmail;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    // Sender address (usually company/warehouse address)
    private String senderName;
    private String senderAddressLine1;
    private String senderAddressLine2;
    private String senderCity;
    private String senderState;
    private String senderPostalCode;
    private String senderCountry;
    private String senderPhone;

    // Additional options
    private boolean signatureRequired;
    private boolean insured;
    private BigDecimal insuranceValue;
    private String specialInstructions;
}
