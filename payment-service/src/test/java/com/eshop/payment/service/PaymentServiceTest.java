package com.eshop.payment.service;

import com.eshop.payment.dto.PaymentRequest;
import com.eshop.payment.dto.PaymentResponse;
import com.eshop.payment.dto.RefundRequest;
import com.eshop.payment.dto.RefundResponse;
import com.eshop.payment.entity.Payment;
import com.eshop.payment.enums.PaymentMethod;
import com.eshop.payment.enums.PaymentStatus;
import com.eshop.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PaymentService
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    private PaymentRequest paymentRequest;
    private Payment payment;

    @BeforeEach
    void setUp() {
        paymentRequest = new PaymentRequest();
        paymentRequest.setOrderId(1L);
        paymentRequest.setUserId(1L);
        paymentRequest.setAmount(new BigDecimal("100.00"));
        paymentRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        paymentRequest.setCardNumber("1234567890123456");
        paymentRequest.setExpiryDate("12/25");
        paymentRequest.setCvv("123");

        payment = new Payment();
        payment.setId(1L);
        payment.setOrderId(1L);
        payment.setUserId(1L);
        payment.setAmount(new BigDecimal("100.00"));
        payment.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setTransactionId("TXN123456");
    }

    @Test
    void testProcessPayment_Success() {
        // Given
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // When
        PaymentResponse response = paymentService.processPayment(paymentRequest);

        // Then
        assertNotNull(response);
        assertEquals(PaymentStatus.SUCCESS, response.getPaymentStatus());
        verify(paymentRepository, times(2)).save(any(Payment.class));
    }

    @Test
    void testGetPaymentStatus_Found() {
        // Given
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        // When
        Optional<PaymentResponse> response = paymentService.getPaymentStatus(1L);

        // Then
        assertTrue(response.isPresent());
        assertEquals(1L, response.get().getId());
        assertEquals(PaymentStatus.SUCCESS, response.get().getPaymentStatus());
    }

    @Test
    void testGetPaymentStatus_NotFound() {
        // Given
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        Optional<PaymentResponse> response = paymentService.getPaymentStatus(1L);

        // Then
        assertFalse(response.isPresent());
    }

    @Test
    void testValidatePaymentMethod_CreditCard_Valid() {
        // When
        boolean result = paymentService.validatePaymentMethod(PaymentMethod.CREDIT_CARD, paymentRequest);

        // Then
        assertTrue(result);
    }

    @Test
    void testValidatePaymentMethod_CreditCard_Invalid() {
        // Given
        paymentRequest.setCardNumber(null);

        // When
        boolean result = paymentService.validatePaymentMethod(PaymentMethod.CREDIT_CARD, paymentRequest);

        // Then
        assertFalse(result);
    }

    @Test
    void testProcessRefund_Success() {
        // Given
        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setPaymentId(1L);
        refundRequest.setAmount(new BigDecimal("50.00"));
        refundRequest.setReason("Customer request");

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // When
        RefundResponse response = paymentService.processRefund(refundRequest);

        // Then
        assertTrue(response.isSuccess());
        assertEquals(1L, response.getPaymentId());
        assertEquals(new BigDecimal("50.00"), response.getRefundAmount());
    }

    @Test
    void testProcessRefund_PaymentNotFound() {
        // Given
        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setPaymentId(1L);
        refundRequest.setAmount(new BigDecimal("50.00"));

        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        RefundResponse response = paymentService.processRefund(refundRequest);

        // Then
        assertFalse(response.isSuccess());
        assertEquals("Payment not found", response.getErrorMessage());
    }

    @Test
    void testCancelPayment_Success() {
        // Given
        Payment pendingPayment = new Payment();
        pendingPayment.setId(1L);
        pendingPayment.setPaymentStatus(PaymentStatus.PENDING);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(pendingPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(pendingPayment);

        // When
        boolean result = paymentService.cancelPayment(1L);

        // Then
        assertTrue(result);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void testCancelPayment_CannotCancel() {
        // Given - payment is already successful, cannot be cancelled
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        // When
        boolean result = paymentService.cancelPayment(1L);

        // Then
        assertFalse(result);
        verify(paymentRepository, never()).save(any(Payment.class));
    }
}
