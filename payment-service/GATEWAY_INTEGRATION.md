# Payment Gateway Integration

This document describes the payment gateway integration implemented in the Payment Service.

## Overview

The Payment Service integrates with Stripe payment gateway to process payments and refunds:
- **Stripe** - Credit/Debit card processing and various payment methods

## Architecture

### Gateway Interface
All payment gateways implement the `PaymentGateway` interface:
```java
public interface PaymentGateway {
    PaymentGatewayResponse processPayment(PaymentGatewayRequest request);
    RefundGatewayResponse processRefund(RefundGatewayRequest request);
    boolean validatePaymentMethod(PaymentGatewayRequest request);
    String getGatewayName();
    boolean supportsPaymentMethod(String paymentMethod);
}
```

### Gateway Factory
The `PaymentGatewayFactory` routes all payment methods to Stripe:
- Credit Card → Stripe
- Debit Card → Stripe  
- Bank Transfer → Stripe
- Mobile Payment → Stripe
- Cryptocurrency → Stripe
- All other methods → Stripe (with appropriate warnings)

## Mock Implementation

The Stripe gateway is implemented as a mock service for development and testing:

### Stripe Mock Gateway
- Simulates Stripe API behavior
- Supports test card numbers for different scenarios
- 95% success rate simulation
- Luhn algorithm validation for card numbers
- Comprehensive error handling and test scenarios

**Test Card Numbers:**
- `4242424242424242` - Success
- `4000000000000002` - Declined (Insufficient funds)
- `4000000000009995` - Declined (Insufficient funds)
- `4000000000009987` - Declined (Lost card)
- `4000000000009979` - Declined (Stolen card)
- `4000000000000069` - Expired card
- `4000000000000127` - Incorrect CVC
- `4000000000000119` - Processing error

## Configuration

```properties
# Gateway Configuration
payment.gateway.mock-mode=true
payment.gateway.timeout=30s
payment.gateway.retry-attempts=3

# Stripe Configuration
payment.gateway.stripe.api-key=sk_test_mock_api_key
payment.gateway.stripe.webhook-secret=whsec_mock_webhook_secret
payment.gateway.stripe.public-key=pk_test_mock_public_key
```

## API Endpoints

### Gateway Testing Endpoints

#### Test Stripe Payment
```http
POST /api/payments/gateway/test/stripe
Content-Type: application/json

{
    "amount": 100.00,
    "cardNumber": "4242424242424242",
    "expiryMonth": "12",
    "expiryYear": "2025",
    "cvv": "123",
    "cardHolderName": "John Doe",
    "customerEmail": "test@example.com"
}
```

#### Test Stripe Refund
```http
POST /api/payments/gateway/test/stripe/refund
Content-Type: application/json

{
    "transactionId": "ch_test123",
    "amount": 50.00,
    "reason": "Customer request"
}
```

#### Validate Payment Method
```http
POST /api/payments/gateway/test/validate/stripe
Content-Type: application/json

{
    "cardNumber": "4242424242424242",
    "expiryMonth": "12",
    "expiryYear": "2025",
    "cvv": "123",
    "cardHolderName": "John Doe"
}
```

#### Get Available Gateways
```http
GET /api/payments/gateway/available
```

## Usage Example

### Processing a Payment
```java
@Autowired
private PaymentService paymentService;

PaymentRequest request = new PaymentRequest();
request.setOrderId(1L);
request.setUserId(1L);
request.setAmount(new BigDecimal("100.00"));
request.setPaymentMethod(PaymentMethod.CREDIT_CARD);
request.setCardNumber("4242424242424242");
request.setExpiryDate("12/25");
request.setCvv("123");

PaymentResponse response = paymentService.processPayment(request);
```

### Processing a Refund
```java
RefundRequest refundRequest = new RefundRequest();
refundRequest.setPaymentId(1L);
refundRequest.setAmount(new BigDecimal("50.00"));
refundRequest.setReason("Customer request");

RefundResponse refundResponse = paymentService.processRefund(refundRequest);
```

## Error Handling

The gateway integration includes comprehensive error handling:
- Invalid payment method details
- Gateway timeouts
- Network errors
- Authentication failures
- Insufficient funds
- Card declined scenarios
- Card validation errors

## Testing

Run the gateway integration tests:
```bash
mvn test -Dtest=PaymentGatewayIntegrationTest
```

## Future Enhancements

1. **Real Gateway Integration**
   - Replace mock implementation with actual Stripe SDK
   - Add webhook handling for payment status updates
   - Implement proper authentication and security

2. **Additional Gateways**
   - PayPal (can be added back easily due to gateway interface)
   - Square
   - Authorize.Net
   - Braintree
   - Apple Pay / Google Pay

3. **Enhanced Features**
   - Payment method tokenization
   - Recurring payments
   - Multi-currency support
   - Fraud detection integration

## Security Considerations

- Never log sensitive payment data (card numbers, CVV)
- Use HTTPS for all gateway communications
- Implement proper PCI DSS compliance
- Store minimal payment information
- Use payment method tokens when possible

## Monitoring

Monitor gateway performance and success rates:
- Payment success/failure rates
- Response times
- Error rates and types
- Transaction volumes

## Support

For gateway-specific issues:
- Stripe: Check Stripe Dashboard and logs
- Mock Mode: Check application logs for detailed error messages
