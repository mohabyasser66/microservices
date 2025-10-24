package com.eshop.payment.controller;

import com.eshop.payment.dto.*;
import com.eshop.payment.entity.Payment;
import com.eshop.payment.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/process")
    public ResponseEntity<PaymentGatewayResponse> processPayment(@RequestBody PaymentGatewayRequest request) {
        log.info("Processing payment request for order: {}, amount: {} {}", 
                request.getOrderId(), request.getAmount(), request.getCurrency());
        
        try {
            // Check if order already has successful payment
            if (request.getOrderId() != null && paymentService.hasSuccessfulPayment(request.getOrderId())) {
                log.warn("Order {} already has a successful payment", request.getOrderId());
                PaymentGatewayResponse response = PaymentGatewayResponse.failure("DUPLICATE_PAYMENT", 
                    "Order already has a successful payment", "SYSTEM");
                return ResponseEntity.badRequest().body(response);
            }
            
            PaymentGatewayResponse response = paymentService.processPayment(request);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error processing payment", e);
            PaymentGatewayResponse errorResponse = PaymentGatewayResponse.failure("PAYMENT_ERROR", 
                "Error processing payment: " + e.getMessage(), "SYSTEM");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    
    @PostMapping("/refund")
    public ResponseEntity<RefundGatewayResponse> processRefund(@RequestBody RefundGatewayRequest request) {
        log.info("Processing refund request for transaction: {}, amount: {}", 
                request.getOriginalTransactionId(), request.getRefundAmount());
        
        try {
            RefundGatewayResponse response = paymentService.processRefund(request);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error processing refund", e);
            RefundGatewayResponse errorResponse = RefundGatewayResponse.failure(
                "Error processing refund: " + e.getMessage(), "SYSTEM");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    
    @GetMapping("/order/{orderId}")
    public ResponseEntity<Payment> getPaymentByOrderId(@PathVariable UUID orderId) {
        log.info("Retrieving payment for order: {}", orderId);
        
        try {
            Payment payment = paymentService.findByOrderId(orderId);
            return ResponseEntity.ok(payment);
            
        } catch (Exception e) {
            log.error("Payment not found for order: {}", orderId, e);
            return ResponseEntity.notFound().build();
        }
    }

    
    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<Payment> getPaymentByTransactionId(@PathVariable String transactionId) {
        log.info("Retrieving payment for transaction: {}", transactionId);
        
        try {
            Payment payment = paymentService.findByTransactionId(transactionId);
            return ResponseEntity.ok(payment);
            
        } catch (Exception e) {
            log.error("Payment not found for transaction: {}", transactionId, e);
            return ResponseEntity.notFound().build();
        }
    }

    
    @GetMapping("/user/{userId}/history")
    public ResponseEntity<List<Payment>> getPaymentHistory(@PathVariable UUID userId) {
        log.info("Retrieving payment history for user: {}", userId);
        
        try {
            List<Payment> payments = paymentService.getPaymentHistory(userId);
            return ResponseEntity.ok(payments);
            
        } catch (Exception e) {
            log.error("Error retrieving payment history for user: {}", userId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validatePayment(@RequestBody PaymentGatewayRequest request) {
        log.info("Validating payment request");
        
        try {
            boolean valid = paymentService.validatePaymentMethod(request);
            String selectedGateway = paymentService.getSelectedGateway(request);
            
            return ResponseEntity.ok(Map.of(
                "valid", valid,
                "selectedGateway", selectedGateway,
                "message", valid ? "Payment request is valid" : "Payment request is invalid",
                "strategyPattern", "Gateway selected automatically based on business rules"
            ));
            
        } catch (Exception e) {
            log.error("Error validating payment", e);
            return ResponseEntity.badRequest().body(Map.of(
                "valid", false,
                "error", e.getMessage()
            ));
        }
    }

    
    @GetMapping("/order/{orderId}/status")
    public ResponseEntity<Map<String, Object>> getOrderPaymentStatus(@PathVariable UUID orderId) {
        log.info("Checking payment status for order: {}", orderId);
        
        try {
            boolean hasPayment = paymentService.hasSuccessfulPayment(orderId);
            
            return ResponseEntity.ok(Map.of(
                "orderId", orderId,
                "hasSuccessfulPayment", hasPayment,
                "message", hasPayment ? "Order has successful payment" : "Order has no successful payment"
            ));
            
        } catch (Exception e) {
            log.error("Error checking payment status for order: {}", orderId, e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        }
    }

    
    @PostMapping("/gateway-selection-demo")
    public ResponseEntity<Map<String, Object>> demonstrateGatewaySelection(@RequestBody PaymentGatewayRequest request) {
        log.info("Demonstrating gateway selection logic");
        
        try {
            String selectedGateway = paymentService.getSelectedGateway(request);
            
            return ResponseEntity.ok(Map.of(
                "selectedGateway", selectedGateway,
                "request", Map.of(
                    "amount", request.getAmount(),
                    "currency", request.getCurrency(),
                    "hasPayPalEmail", request.getPaymentDetails() != null && 
                                    request.getPaymentDetails().containsKey("paypal_email"),
                    "hasCardDetails", request.getPaymentDetails() != null && 
                                    request.getPaymentDetails().containsKey("card_number")
                ),
                "explanation", "Strategy Pattern automatically selects the optimal gateway based on business rules",
                "businessRules", Map.of(
                    "paypalEmail", "If PayPal email provided → PayPal Gateway",
                    "cardDetails", "If card details provided → Stripe Gateway", 
                    "highAmount", "Amount > $1000 → Stripe Gateway (better processing)",
                    "eurCurrency", "EUR currency → PayPal Gateway (better EU support)",
                    "default", "Stripe Gateway as primary"
                )
            ));
            
        } catch (Exception e) {
            log.error("Error demonstrating gateway selection", e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        }
    }

    
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getPaymentStatus() {
        log.info("Getting payment service status");
        
        try {
            return ResponseEntity.ok(Map.of(
                "service", "Payment Service with Strategy Pattern + Database",
                "availableGateways", new String[]{"STRIPE", "PAYPAL"},
                "features", new String[]{
                    "Intelligent gateway selection",
                    "Database persistence", 
                    "Payment history tracking",
                    "Refund management",
                    "Duplicate payment prevention",
                    "Strategy Pattern implementation"
                },
                "strategyPattern", true,
                "databasePersistence", true,
                "gatewaySelection", "Automatic based on business rules",
                "status", "Running"
            ));
            
        } catch (Exception e) {
            log.error("Error getting payment status", e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to get payment status: " + e.getMessage()
            ));
        }
    }

}
