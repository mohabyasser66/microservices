package com.eshop.payment.repository;

import com.eshop.payment.entity.Payment;
import com.eshop.payment.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByOrderId(UUID orderId);

    List<Payment> findByUserId(UUID userId);

    Page<Payment> findByUserId(UUID userId, Pageable pageable);

    Optional<Payment> findByTransactionId(String transactionId);

    List<Payment> findByPaymentStatus(PaymentStatus status);

    Page<Payment> findByPaymentStatus(PaymentStatus status, Pageable pageable);

    List<Payment> findByUserIdAndPaymentStatus(UUID userId, PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    List<Payment> findPaymentsBetweenDates(@Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);

    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    Page<Payment> findPaymentsBetweenDates(@Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate, 
                                          Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE p.userId = :userId AND p.paymentStatus = 'SUCCESS' " +
           "AND (p.refundedAmount IS NULL OR p.refundedAmount < p.amount)")
    List<Payment> findRefundablePaymentsByUserId(@Param("userId") UUID userId);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.userId = :userId AND p.paymentStatus = 'SUCCESS'")
    BigDecimal calculateTotalPaymentsByUserId(@Param("userId") UUID userId);

    @Query("SELECT SUM(p.refundedAmount) FROM Payment p WHERE p.userId = :userId AND p.refundedAmount > 0")
    BigDecimal calculateTotalRefundsByUserId(@Param("userId") UUID userId);

    @Query("SELECT p FROM Payment p WHERE p.paymentStatus = 'FAILED' AND p.createdAt > :cutoffDate")
    List<Payment> findFailedPaymentsAfterDate(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("SELECT p FROM Payment p WHERE p.paymentStatus = 'PENDING' AND p.createdAt < :cutoffDate")
    List<Payment> findStuckPendingPayments(@Param("cutoffDate") LocalDateTime cutoffDate);

    long countByPaymentStatus(PaymentStatus status);

    long countByUserIdAndPaymentStatus(UUID userId, PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.amount > :amount AND p.paymentStatus = 'SUCCESS'")
    List<Payment> findHighValuePayments(@Param("amount") BigDecimal amount);

    @Query("SELECT p FROM Payment p WHERE p.userId = :userId ORDER BY p.createdAt DESC")
    List<Payment> findRecentPaymentsByUserId(@Param("userId") UUID userId, Pageable pageable);

    boolean existsByOrderIdAndPaymentStatus(UUID orderId, PaymentStatus status);

    List<Payment> findByOrderIdIn(List<UUID> orderIds);
}
