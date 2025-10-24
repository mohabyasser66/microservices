package com.eshop.payment.dto;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PaymentGatewayRequest {
    
    private UUID orderId;
    private UUID userId;
    private BigDecimal amount;
    private String currency = "USD";
    private String paymentMethod;
    private Map<String, String> paymentDetails = new HashMap<>();
    private String description;
    private String customerEmail;
    
    
    public PaymentGatewayRequest(UUID orderId, UUID userId, BigDecimal amount, String paymentMethod) {
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }
    
    
    public void setCardDetails(String cardNumber, String expiryMonth, String expiryYear, String cvv, String cardHolderName) {
        paymentDetails.put("card_number", cardNumber);
        paymentDetails.put("expiry_month", expiryMonth);
        paymentDetails.put("expiry_year", expiryYear);
        paymentDetails.put("cvv", cvv);
        paymentDetails.put("card_holder_name", cardHolderName);
    }
    
    public void setPayPalDetails(String paypalEmail) {
        paymentDetails.put("paypal_email", paypalEmail);
    }
    
}
