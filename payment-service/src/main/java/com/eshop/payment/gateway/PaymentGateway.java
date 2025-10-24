package com.eshop.payment.gateway;

import com.eshop.payment.dto.PaymentGatewayRequest;
import com.eshop.payment.dto.PaymentGatewayResponse;
import com.eshop.payment.dto.RefundGatewayRequest;
import com.eshop.payment.dto.RefundGatewayResponse;

public interface PaymentGateway {

    PaymentGatewayResponse processPayment(PaymentGatewayRequest request);
   
    RefundGatewayResponse processRefund(RefundGatewayRequest request);
    
    boolean validatePaymentMethod(PaymentGatewayRequest request);
    
    String getGatewayName();
    
    boolean supportsPaymentMethod(String paymentMethod);
}
