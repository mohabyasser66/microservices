package com.eshop.payment.service;

import com.eshop.payment.dto.*;
import com.eshop.payment.entity.Payment;
import com.eshop.payment.enums.PaymentMethod;
import com.eshop.payment.enums.PaymentStatus;
import com.eshop.payment.gateway.*;
import com.eshop.payment.gateway.factory.PaymentGatewayFactory;
import com.eshop.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Payment service implementation with comprehensive payment processing capabilities
 */
@Service
@Transactional
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentGatewayFactory gatewayFactory;

    /**
     * Process a payment request
     */
    public PaymentResponse processPayment(PaymentRequest request) {
        logger.info("Processing payment for order: {} with amount: {}", request.getOrderId(), request.getAmount());
        
        try {
            // Validate payment method
            if (!validatePaymentMethod(request.getPaymentMethod(), request)) {
                return createFailedPaymentResponse("Invalid payment method details");
            }

            // Create payment entity
            Payment payment = new Payment(request.getOrderId(), request.getUserId(), 
                                        request.getAmount(), request.getPaymentMethod());
            payment.setPaymentStatus(PaymentStatus.PROCESSING);
            
            // Save initial payment record
            payment = paymentRepository.save(payment);
            
            // Process payment with external gateway
            PaymentGatewayResponse gatewayResponse = processWithGateway(request);
            
            // Update payment based on gateway response
            updatePaymentFromGatewayResponse(payment, gatewayResponse);
            
            // Save updated payment
            payment = paymentRepository.save(payment);
            
            logger.info("Payment processed successfully: {}", payment.getId());
            return convertToPaymentResponse(payment);
            
        } catch (Exception e) {
            logger.error("Error processing payment for order: {}", request.getOrderId(), e);
            return createFailedPaymentResponse("Payment processing failed: " + e.getMessage());
        }
    }

    /**
     * Get payment status by payment ID
     */
    @Transactional(readOnly = true)
    public Optional<PaymentResponse> getPaymentStatus(Long paymentId) {
        logger.info("Getting payment status for payment ID: {}", paymentId);
        
        Optional<Payment> payment = paymentRepository.findById(paymentId);
        return payment.map(this::convertToPaymentResponse);
    }

    /**
     * Get payment by order ID
     */
    @Transactional(readOnly = true)
    public Optional<PaymentResponse> getPaymentByOrderId(Long orderId) {
        logger.info("Getting payment for order ID: {}", orderId);
        
        Optional<Payment> payment = paymentRepository.findByOrderId(orderId);
        return payment.map(this::convertToPaymentResponse);
    }

    /**
     * Process a refund request
     */
    public RefundResponse processRefund(RefundRequest request) {
        logger.info("Processing refund for payment ID: {} with amount: {}", 
                   request.getPaymentId(), request.getAmount());
        
        try {
            Optional<Payment> paymentOpt = paymentRepository.findById(request.getPaymentId());
            if (paymentOpt.isEmpty()) {
                return createFailedRefundResponse("Payment not found");
            }
            
            Payment payment = paymentOpt.get();
            
            // Validate refund eligibility
            if (!payment.isRefundable()) {
                return createFailedRefundResponse("Payment is not refundable");
            }
            
            // Determine refund amount
            BigDecimal refundAmount = request.getAmount() != null ? 
                request.getAmount() : payment.getRefundableAmount();
            
            // Validate refund amount
            if (refundAmount.compareTo(payment.getRefundableAmount()) > 0) {
                return createFailedRefundResponse("Refund amount exceeds refundable amount");
            }
            
            // Process refund with gateway
            RefundGatewayResponse gatewayResponse = processRefundWithGateway(payment, refundAmount);
            
            if (gatewayResponse.isSuccess()) {
                // Update payment with refund information
                BigDecimal newRefundedAmount = (payment.getRefundedAmount() != null ? 
                    payment.getRefundedAmount() : BigDecimal.ZERO).add(refundAmount);
                
                payment.setRefundedAmount(newRefundedAmount);
                
                // Update payment status
                if (newRefundedAmount.compareTo(payment.getAmount()) >= 0) {
                    payment.setPaymentStatus(PaymentStatus.REFUNDED);
                } else {
                    payment.setPaymentStatus(PaymentStatus.PARTIALLY_REFUNDED);
                }
                
                payment.setUpdatedAt(LocalDateTime.now());
                paymentRepository.save(payment);
                
                logger.info("Refund processed successfully for payment: {}", payment.getId());
                return createSuccessfulRefundResponse(payment, refundAmount, gatewayResponse.getTransactionId());
            } else {
                return createFailedRefundResponse("Gateway refund failed: " + gatewayResponse.getErrorMessage());
            }
            
        } catch (Exception e) {
            logger.error("Error processing refund for payment: {}", request.getPaymentId(), e);
            return createFailedRefundResponse("Refund processing failed: " + e.getMessage());
        }
    }

    /**
     * Validate payment method details
     */
    public boolean validatePaymentMethod(PaymentMethod method, PaymentRequest request) {
        logger.debug("Validating payment method: {}", method);
        
        // Get appropriate gateway for validation
        PaymentGateway gateway = gatewayFactory.getGateway(method);
        
        // Create gateway request for validation
        PaymentGatewayRequest gatewayRequest = createGatewayRequest(request);
        
        return gateway.validatePaymentMethod(gatewayRequest);
    }

    /**
     * Cancel a payment
     */
    public boolean cancelPayment(Long paymentId) {
        logger.info("Cancelling payment: {}", paymentId);
        
        try {
            Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
            if (paymentOpt.isEmpty()) {
                logger.warn("Payment not found for cancellation: {}", paymentId);
                return false;
            }
            
            Payment payment = paymentOpt.get();
            
            // Check if payment can be cancelled
            if (payment.getPaymentStatus() == PaymentStatus.SUCCESS || 
                payment.getPaymentStatus() == PaymentStatus.REFUNDED) {
                logger.warn("Cannot cancel completed payment: {}", paymentId);
                return false;
            }
            
            payment.setPaymentStatus(PaymentStatus.CANCELLED);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);
            
            logger.info("Payment cancelled successfully: {}", paymentId);
            return true;
            
        } catch (Exception e) {
            logger.error("Error cancelling payment: {}", paymentId, e);
            return false;
        }
    }

    /**
     * Get payments by user ID
     */
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByUserId(Long userId) {
        logger.info("Getting payments for user: {}", userId);
        
        List<Payment> payments = paymentRepository.findByUserId(userId);
        return payments.stream()
                      .map(this::convertToPaymentResponse)
                      .collect(Collectors.toList());
    }

    /**
     * Get payments by user ID with pagination
     */
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getPaymentsByUserId(Long userId, Pageable pageable) {
        logger.info("Getting payments for user: {} with pagination", userId);
        
        Page<Payment> payments = paymentRepository.findByUserId(userId, pageable);
        return payments.map(this::convertToPaymentResponse);
    }

    /**
     * Get payments by status
     */
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByStatus(PaymentStatus status) {
        logger.info("Getting payments with status: {}", status);
        
        List<Payment> payments = paymentRepository.findByPaymentStatus(status);
        return payments.stream()
                      .map(this::convertToPaymentResponse)
                      .collect(Collectors.toList());
    }

    /**
     * Calculate total payments by user
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalPaymentsByUser(Long userId) {
        logger.info("Calculating total payments for user: {}", userId);
        
        BigDecimal total = paymentRepository.calculateTotalPaymentsByUserId(userId);
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * Get refundable payments by user
     */
    @Transactional(readOnly = true)
    public List<PaymentResponse> getRefundablePaymentsByUser(Long userId) {
        logger.info("Getting refundable payments for user: {}", userId);
        
        List<Payment> payments = paymentRepository.findRefundablePaymentsByUserId(userId);
        return payments.stream()
                      .map(this::convertToPaymentResponse)
                      .collect(Collectors.toList());
    }

    /**
     * Get failed payments for retry
     */
    @Transactional(readOnly = true)
    public List<PaymentResponse> getFailedPaymentsForRetry(int hours) {
        logger.info("Getting failed payments from last {} hours for retry", hours);
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusHours(hours);
        List<Payment> payments = paymentRepository.findFailedPaymentsAfterDate(cutoffDate);
        return payments.stream()
                      .map(this::convertToPaymentResponse)
                      .collect(Collectors.toList());
    }

    /**
     * Cleanup stuck pending payments
     */
    public int cleanupStuckPendingPayments(int hoursOld) {
        logger.info("Cleaning up pending payments older than {} hours", hoursOld);
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusHours(hoursOld);
        List<Payment> stuckPayments = paymentRepository.findStuckPendingPayments(cutoffDate);
        
        int updatedCount = 0;
        for (Payment payment : stuckPayments) {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Payment timeout - automatically failed");
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);
            updatedCount++;
        }
        
        logger.info("Cleaned up {} stuck pending payments", updatedCount);
        return updatedCount;
    }

    // Private helper methods

    private PaymentGatewayResponse processWithGateway(PaymentRequest request) {
        logger.debug("Processing payment with gateway for method: {}", request.getPaymentMethod());
        
        try {
            // Get appropriate gateway
            PaymentGateway gateway = gatewayFactory.getGateway(request.getPaymentMethod());
            
            // Create gateway request
            PaymentGatewayRequest gatewayRequest = createGatewayRequest(request);
            
            // Process payment
            return gateway.processPayment(gatewayRequest);
            
        } catch (Exception e) {
            logger.error("Error processing payment with gateway", e);
            return PaymentGatewayResponse.failure("GATEWAY_ERROR", 
                "Payment gateway processing failed: " + e.getMessage(), "UNKNOWN");
        }
    }

    private RefundGatewayResponse processRefundWithGateway(Payment payment, BigDecimal amount) {
        logger.debug("Processing refund with gateway for payment: {}", payment.getId());
        
        try {
            // Get appropriate gateway
            PaymentGateway gateway = gatewayFactory.getGateway(payment.getPaymentMethod());
            
            // Create refund request
            RefundGatewayRequest refundRequest = new RefundGatewayRequest();
            refundRequest.setOriginalTransactionId(payment.getTransactionId());
            refundRequest.setGatewayTransactionId(payment.getGatewayResponseCode());
            refundRequest.setRefundAmount(amount);
            refundRequest.setPaymentId(payment.getId());
            refundRequest.setGatewayName(gateway.getGatewayName());
            
            // Process refund
            return gateway.processRefund(refundRequest);
            
        } catch (Exception e) {
            logger.error("Error processing refund with gateway", e);
            return RefundGatewayResponse.failure("GATEWAY_ERROR", "UNKNOWN");
        }
    }

    private PaymentGatewayRequest createGatewayRequest(PaymentRequest request) {
        PaymentGatewayRequest gatewayRequest = new PaymentGatewayRequest();
        gatewayRequest.setOrderId(request.getOrderId());
        gatewayRequest.setUserId(request.getUserId());
        gatewayRequest.setAmount(request.getAmount());
        gatewayRequest.setPaymentMethod(request.getPaymentMethod().name());
        gatewayRequest.setDescription("Order #" + request.getOrderId() + " payment");
        
        // Card details
        gatewayRequest.setCardNumber(request.getCardNumber());
        gatewayRequest.setCardHolderName(request.getCardHolderName());
        gatewayRequest.setCvv(request.getCvv());
        
        // Parse expiry date if present
        if (request.getExpiryDate() != null && request.getExpiryDate().contains("/")) {
            String[] parts = request.getExpiryDate().split("/");
            if (parts.length == 2) {
                gatewayRequest.setExpiryMonth(parts[0].trim());
                gatewayRequest.setExpiryYear(parts[1].trim());
            }
        }
        
        // PayPal details
        gatewayRequest.setPaypalEmail(request.getPaypalEmail());
        
        // Bank transfer details
        gatewayRequest.setBankAccountNumber(request.getBankAccountNumber());
        gatewayRequest.setRoutingNumber(request.getRoutingNumber());
        
        return gatewayRequest;
    }

    private void updatePaymentFromGatewayResponse(Payment payment, PaymentGatewayResponse response) {
        payment.setGatewayResponseCode(response.getResponseCode());
        payment.setGatewayResponseMessage(response.getResponseMessage());
        
        if (response.isSuccess()) {
            payment.setPaymentStatus(PaymentStatus.SUCCESS);
            payment.setTransactionId(response.getTransactionId());
            payment.setProcessedAt(LocalDateTime.now());
        } else {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setFailureReason(response.getResponseMessage());
        }
        
        payment.setUpdatedAt(LocalDateTime.now());
    }

    private PaymentResponse convertToPaymentResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setOrderId(payment.getOrderId());
        response.setUserId(payment.getUserId());
        response.setAmount(payment.getAmount());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setPaymentStatus(payment.getPaymentStatus());
        response.setTransactionId(payment.getTransactionId());
        response.setGatewayResponseCode(payment.getGatewayResponseCode());
        response.setGatewayResponseMessage(payment.getGatewayResponseMessage());
        response.setRefundedAmount(payment.getRefundedAmount());
        response.setFailureReason(payment.getFailureReason());
        response.setCreatedAt(payment.getCreatedAt());
        response.setUpdatedAt(payment.getUpdatedAt());
        response.setProcessedAt(payment.getProcessedAt());
        response.setRefundable(payment.isRefundable());
        response.setRefundableAmount(payment.getRefundableAmount());
        return response;
    }

    private PaymentResponse createFailedPaymentResponse(String errorMessage) {
        PaymentResponse response = new PaymentResponse();
        response.setPaymentStatus(PaymentStatus.FAILED);
        response.setFailureReason(errorMessage);
        return response;
    }

    private RefundResponse createSuccessfulRefundResponse(Payment payment, BigDecimal refundAmount, String transactionId) {
        RefundResponse response = new RefundResponse(true);
        response.setPaymentId(payment.getId());
        response.setRefundAmount(refundAmount);
        response.setTotalRefundedAmount(payment.getRefundedAmount());
        response.setRefundTransactionId(transactionId);
        response.setStatus(payment.getPaymentStatus().name());
        response.setRefundedAt(LocalDateTime.now());
        return response;
    }

    private RefundResponse createFailedRefundResponse(String errorMessage) {
        RefundResponse response = new RefundResponse(false);
        response.setErrorMessage(errorMessage);
        return response;
    }

    private String generateTransactionId() {
        return "TXN_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}
