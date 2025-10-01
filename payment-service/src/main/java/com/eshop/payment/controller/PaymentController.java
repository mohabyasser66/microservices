package com.eshop.payment.controller;

import com.eshop.payment.dto.*;
import com.eshop.payment.enums.PaymentStatus;
import com.eshop.payment.service.PaymentService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Payment controller providing comprehensive payment management endpoints
 */
@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private PaymentService paymentService;

    /**
     * Process a new payment
     */
    @PostMapping("/process")
    @CircuitBreaker(name = "payment-service", fallbackMethod = "processPaymentFallback")
    @Retry(name = "payment-service")
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
        logger.info("Processing payment request for order: {}", request.getOrderId());
        
        try {
            PaymentResponse response = paymentService.processPayment(request);
            
            if (response.getPaymentStatus() == PaymentStatus.SUCCESS) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            logger.error("Error processing payment for order: {}", request.getOrderId(), e);
            PaymentResponse errorResponse = new PaymentResponse();
            errorResponse.setPaymentStatus(PaymentStatus.FAILED);
            errorResponse.setFailureReason("Payment processing error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get payment status by payment ID
     */
    @GetMapping("/{paymentId}")
    @CircuitBreaker(name = "payment-service", fallbackMethod = "getPaymentStatusFallback")
    public ResponseEntity<PaymentResponse> getPaymentStatus(@PathVariable Long paymentId) {
        logger.info("Getting payment status for payment ID: {}", paymentId);
        
        Optional<PaymentResponse> payment = paymentService.getPaymentStatus(paymentId);
        return payment.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get payment by order ID
     */
    @GetMapping("/order/{orderId}")
    @CircuitBreaker(name = "payment-service", fallbackMethod = "getPaymentByOrderIdFallback")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(@PathVariable Long orderId) {
        logger.info("Getting payment for order ID: {}", orderId);
        
        Optional<PaymentResponse> payment = paymentService.getPaymentByOrderId(orderId);
        return payment.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Process a refund
     */
    @PostMapping("/refund")
    @CircuitBreaker(name = "payment-service", fallbackMethod = "processRefundFallback")
    @Retry(name = "payment-service")
    public ResponseEntity<RefundResponse> processRefund(@Valid @RequestBody RefundRequest request) {
        logger.info("Processing refund request for payment ID: {}", request.getPaymentId());
        
        try {
            RefundResponse response = paymentService.processRefund(request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            logger.error("Error processing refund for payment: {}", request.getPaymentId(), e);
            RefundResponse errorResponse = new RefundResponse(false);
            errorResponse.setErrorMessage("Refund processing error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Cancel a payment
     */
    @PutMapping("/{paymentId}/cancel")
    @CircuitBreaker(name = "payment-service", fallbackMethod = "cancelPaymentFallback")
    public ResponseEntity<String> cancelPayment(@PathVariable Long paymentId) {
        logger.info("Cancelling payment: {}", paymentId);
        
        try {
            boolean cancelled = paymentService.cancelPayment(paymentId);
            
            if (cancelled) {
                return ResponseEntity.ok("Payment cancelled successfully");
            } else {
                return ResponseEntity.badRequest().body("Payment cannot be cancelled");
            }
        } catch (Exception e) {
            logger.error("Error cancelling payment: {}", paymentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Error cancelling payment: " + e.getMessage());
        }
    }

    /**
     * Validate payment method
     */
    @PostMapping("/validate-method")
    public ResponseEntity<Boolean> validatePaymentMethod(@Valid @RequestBody PaymentRequest request) {
        logger.info("Validating payment method: {}", request.getPaymentMethod());
        
        try {
            boolean valid = paymentService.validatePaymentMethod(request.getPaymentMethod(), request);
            return ResponseEntity.ok(valid);
        } catch (Exception e) {
            logger.error("Error validating payment method", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    /**
     * Get payments by user ID
     */
    @GetMapping("/user/{userId}")
    @CircuitBreaker(name = "payment-service", fallbackMethod = "getPaymentsByUserIdFallback")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByUserId(@PathVariable Long userId) {
        logger.info("Getting payments for user: {}", userId);
        
        try {
            List<PaymentResponse> payments = paymentService.getPaymentsByUserId(userId);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            logger.error("Error getting payments for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get payments by user ID with pagination
     */
    @GetMapping("/user/{userId}/paginated")
    @CircuitBreaker(name = "payment-service", fallbackMethod = "getPaymentsByUserIdPaginatedFallback")
    public ResponseEntity<Page<PaymentResponse>> getPaymentsByUserId(@PathVariable Long userId, 
                                                                   Pageable pageable) {
        logger.info("Getting payments for user: {} with pagination", userId);
        
        try {
            Page<PaymentResponse> payments = paymentService.getPaymentsByUserId(userId, pageable);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            logger.error("Error getting payments for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get payments by status
     */
    @GetMapping("/status/{status}")
    @CircuitBreaker(name = "payment-service", fallbackMethod = "getPaymentsByStatusFallback")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByStatus(@PathVariable PaymentStatus status) {
        logger.info("Getting payments with status: {}", status);
        
        try {
            List<PaymentResponse> payments = paymentService.getPaymentsByStatus(status);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            logger.error("Error getting payments by status: {}", status, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Calculate total payments by user
     */
    @GetMapping("/user/{userId}/total")
    @CircuitBreaker(name = "payment-service", fallbackMethod = "calculateTotalPaymentsFallback")
    public ResponseEntity<BigDecimal> calculateTotalPaymentsByUser(@PathVariable Long userId) {
        logger.info("Calculating total payments for user: {}", userId);
        
        try {
            BigDecimal total = paymentService.calculateTotalPaymentsByUser(userId);
            return ResponseEntity.ok(total);
        } catch (Exception e) {
            logger.error("Error calculating total payments for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(BigDecimal.ZERO);
        }
    }

    /**
     * Get refundable payments by user
     */
    @GetMapping("/user/{userId}/refundable")
    @CircuitBreaker(name = "payment-service", fallbackMethod = "getRefundablePaymentsFallback")
    public ResponseEntity<List<PaymentResponse>> getRefundablePaymentsByUser(@PathVariable Long userId) {
        logger.info("Getting refundable payments for user: {}", userId);
        
        try {
            List<PaymentResponse> payments = paymentService.getRefundablePaymentsByUser(userId);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            logger.error("Error getting refundable payments for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get failed payments for retry (admin endpoint)
     */
    @GetMapping("/admin/failed-payments")
    @CircuitBreaker(name = "payment-service", fallbackMethod = "getFailedPaymentsFallback")
    public ResponseEntity<List<PaymentResponse>> getFailedPaymentsForRetry(
            @RequestParam(defaultValue = "24") int hours) {
        logger.info("Getting failed payments from last {} hours", hours);
        
        try {
            List<PaymentResponse> payments = paymentService.getFailedPaymentsForRetry(hours);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            logger.error("Error getting failed payments", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Cleanup stuck pending payments (admin endpoint)
     */
    @PostMapping("/admin/cleanup-pending")
    public ResponseEntity<String> cleanupStuckPendingPayments(
            @RequestParam(defaultValue = "2") int hoursOld) {
        logger.info("Cleaning up pending payments older than {} hours", hoursOld);
        
        try {
            int cleanedUp = paymentService.cleanupStuckPendingPayments(hoursOld);
            return ResponseEntity.ok("Cleaned up " + cleanedUp + " stuck payments");
        } catch (Exception e) {
            logger.error("Error cleaning up stuck payments", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Error cleaning up payments: " + e.getMessage());
        }
    }

    // Circuit breaker fallback methods

    public ResponseEntity<PaymentResponse> processPaymentFallback(PaymentRequest request, Exception ex) {
        logger.warn("Payment service unavailable, using fallback for order: {}", request.getOrderId(), ex);
        PaymentResponse response = new PaymentResponse();
        response.setPaymentStatus(PaymentStatus.FAILED);
        response.setFailureReason("Payment service temporarily unavailable");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    public ResponseEntity<PaymentResponse> getPaymentStatusFallback(Long paymentId, Exception ex) {
        logger.warn("Payment service unavailable, using fallback for payment: {}", paymentId, ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }

    public ResponseEntity<PaymentResponse> getPaymentByOrderIdFallback(Long orderId, Exception ex) {
        logger.warn("Payment service unavailable, using fallback for order: {}", orderId, ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }

    public ResponseEntity<RefundResponse> processRefundFallback(RefundRequest request, Exception ex) {
        logger.warn("Payment service unavailable, using fallback for refund: {}", request.getPaymentId(), ex);
        RefundResponse response = new RefundResponse(false);
        response.setErrorMessage("Refund service temporarily unavailable");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    public ResponseEntity<String> cancelPaymentFallback(Long paymentId, Exception ex) {
        logger.warn("Payment service unavailable, using fallback for cancel: {}", paymentId, ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                            .body("Payment service temporarily unavailable");
    }

    public ResponseEntity<List<PaymentResponse>> getPaymentsByUserIdFallback(Long userId, Exception ex) {
        logger.warn("Payment service unavailable, using fallback for user payments: {}", userId, ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }

    public ResponseEntity<Page<PaymentResponse>> getPaymentsByUserIdPaginatedFallback(Long userId, 
                                                                                     Pageable pageable, 
                                                                                     Exception ex) {
        logger.warn("Payment service unavailable, using fallback for user payments paginated: {}", userId, ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }

    public ResponseEntity<List<PaymentResponse>> getPaymentsByStatusFallback(PaymentStatus status, Exception ex) {
        logger.warn("Payment service unavailable, using fallback for status: {}", status, ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }

    public ResponseEntity<BigDecimal> calculateTotalPaymentsFallback(Long userId, Exception ex) {
        logger.warn("Payment service unavailable, using fallback for total payments: {}", userId, ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(BigDecimal.ZERO);
    }

    public ResponseEntity<List<PaymentResponse>> getRefundablePaymentsFallback(Long userId, Exception ex) {
        logger.warn("Payment service unavailable, using fallback for refundable payments: {}", userId, ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }

    public ResponseEntity<List<PaymentResponse>> getFailedPaymentsFallback(int hours, Exception ex) {
        logger.warn("Payment service unavailable, using fallback for failed payments", ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
}
