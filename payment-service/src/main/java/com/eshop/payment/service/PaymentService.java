package com.eshop.payment.service;

import com.eshop.payment.dto.*;
import com.eshop.payment.entity.Payment;
import com.eshop.payment.enums.PaymentMethod;
import com.eshop.payment.enums.PaymentStatus;
import com.eshop.payment.gateway.PaymentStrategyContext;
import com.eshop.payment.repository.PaymentRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentStrategyContext strategyContext;

    public PaymentGatewayResponse processPayment(PaymentGatewayRequest request) {
        log.info("Processing payment for order: {}, amount: {} {}", 
                request.getOrderId(), request.getAmount(), request.getCurrency());

        // Create initial payment record
        Payment payment = createInitialPayment(request);
        payment = paymentRepository.save(payment);
        
        try {
            // Use Strategy Pattern to process payment
            PaymentGatewayResponse response = strategyContext.processPayment(request);
            
            // Update payment record with gateway response
            updatePaymentFromGatewayResponse(payment, response);
            paymentRepository.save(payment);
            
            log.info("Payment processed successfully using {} gateway: {}", 
                    response.getGatewayName(), response.getTransactionId());
            return response;
            
        } catch (Exception e) {
            log.error("Payment processing failed for order: {}", request.getOrderId(), e);
            
            // Update payment as failed
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setFailureReason(e.getMessage());
            paymentRepository.save(payment);
            
            throw new RuntimeException("Payment processing failed: " + e.getMessage(), e);
        }
    }

    public RefundGatewayResponse processRefund(RefundGatewayRequest request) {
        log.info("Processing refund for transaction: {}, amount: {}", 
                request.getOriginalTransactionId(), request.getRefundAmount());

        // Find original payment
        Payment originalPayment = paymentRepository.findByTransactionId(request.getOriginalTransactionId())
                .orElseThrow(() -> new RuntimeException("Original payment not found: " + request.getOriginalTransactionId()));

        // Validate refund is possible
        if (originalPayment.getPaymentStatus() != PaymentStatus.SUCCESS) {
            throw new RuntimeException("Cannot refund payment with status: " + originalPayment.getPaymentStatus());
        }

        try {
            RefundGatewayResponse response = strategyContext.processRefund(request);
            
            // Update payment record with refund info
            if (response.isSuccess()) {
                originalPayment.setRefundedAmount(request.getRefundAmount());
                originalPayment.setRefundTransactionId(response.getRefundTransactionId());
                originalPayment.setPaymentStatus(PaymentStatus.REFUNDED);
                originalPayment.setUpdatedAt(LocalDateTime.now());
                paymentRepository.save(originalPayment);
                
                log.info("Refund processed successfully using {} gateway: {}", 
                        response.getGatewayName(), response.getRefundTransactionId());
            }
            
            return response;
            
        } catch (Exception e) {
            log.error("Refund processing failed for transaction: {}", request.getOriginalTransactionId(), e);
            throw new RuntimeException("Refund processing failed: " + e.getMessage(), e);
        }
    }

    public Payment findByOrderId(UUID orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderId));
    }

    public Payment findByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Payment not found for transaction: " + transactionId));
    }

    public List<Payment> getPaymentHistory(UUID userId) {
        return paymentRepository.findByUserId(userId);
    }

    public boolean hasSuccessfulPayment(UUID orderId) {
        return paymentRepository.existsByOrderIdAndPaymentStatus(orderId, PaymentStatus.SUCCESS);
    }

    public boolean validatePaymentMethod(PaymentGatewayRequest request) {
        try {
            return strategyContext.validatePaymentMethod(request);
        } catch (Exception e) {
            log.error("Payment validation failed", e);
            return false;
        }
    }

    public String getSelectedGateway(PaymentGatewayRequest request) {
        return strategyContext.getSelectedGatewayName(request);
    }

    // Helper methods
    private Payment createInitialPayment(PaymentGatewayRequest request) {
        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setUserId(request.getUserId());
        payment.setAmount(request.getAmount());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        
        // Determine payment method from request and convert to enum
        PaymentMethod paymentMethod = determinePaymentMethod(request);
        payment.setPaymentMethod(paymentMethod);
        
        return payment;
    }
    
    private void updatePaymentFromGatewayResponse(Payment payment, PaymentGatewayResponse response) {
        payment.setTransactionId(response.getTransactionId());
        payment.setProcessedAt(response.getProcessedAt());
        payment.setUpdatedAt(LocalDateTime.now());
        
        if (response.isSuccess()) {
            payment.setPaymentStatus(PaymentStatus.SUCCESS);
            payment.setGatewayResponseCode("SUCCESS");
            payment.setGatewayResponseMessage(response.getMessage());
        } else {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setGatewayResponseCode(response.getErrorCode());
            payment.setGatewayResponseMessage(response.getMessage());
            payment.setFailureReason(response.getMessage());
        }
    }
    
    private PaymentMethod determinePaymentMethod(PaymentGatewayRequest request) {
        if (request.getPaymentDetails() != null) {
            if (request.getPaymentDetails().containsKey("paypal_email")) {
                return PaymentMethod.PAYPAL;
            } else if (request.getPaymentDetails().containsKey("card_number")) {
                return PaymentMethod.CREDIT_CARD;
            } else if(request.getPaymentDetails().containsKey("stripe")) {
                return PaymentMethod.STRIPE;
            } else if(request.getPaymentDetails().containsKey("cash")) {
                return PaymentMethod.CASH_ON_DELIVERY;
            }
        }
        return PaymentMethod.CREDIT_CARD; // Default
    }
}
