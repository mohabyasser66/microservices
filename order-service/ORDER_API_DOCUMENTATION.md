# Order Management System - Complete API Documentation

## Overview
The Order Service now provides a comprehensive e-commerce order management system with full CRUD operations, status management, payment processing, and analytics capabilities.

## Features Implemented

### 🔹 **Core CRUD Operations**
- ✅ **Create Order**: Place new orders with full validation
- ✅ **Read Orders**: Get orders by ID, order number, user ID, or all orders
- ✅ **Update Order**: Update order details (addresses, notes, shipping info)
- ✅ **Delete Order**: Delete pending orders

### 🔹 **Order Status Management**
- ✅ **Confirm Order**: Move from PENDING to CONFIRMED
- ✅ **Start Processing**: Move to PROCESSING status
- ✅ **Mark as Shipped**: Set SHIPPED status with tracking number
- ✅ **Mark as Delivered**: Set DELIVERED status
- ✅ **Cancel Order**: Cancel orders with reason
- ✅ **Refund Order**: Process refunds with amount and reason
- ✅ **Return Order**: Handle returns

### 🔹 **Payment Management**
- ✅ **Process Payment**: Start payment processing
- ✅ **Complete Payment**: Mark payment as completed
- ✅ **Failed Payment**: Handle payment failures
- ✅ **Refund Payment**: Process payment refunds

### 🔹 **Search & Filter Operations**
- ✅ **By Status**: Filter orders by OrderStatus
- ✅ **By Payment Status**: Filter by PaymentStatus
- ✅ **By Date Range**: Find orders within date range
- ✅ **By Customer**: Search by customer name

### 🔹 **Business Logic Operations**
- ✅ **Calculate Total**: Calculate order totals
- ✅ **Validation Checks**: Can cancel/ship/refund checks
- ✅ **Inventory Integration**: Stock validation before order creation

### 🔹 **Analytics & Reporting**
- ✅ **Total Order Count**: Get total number of orders
- ✅ **Total Revenue**: Calculate total revenue from completed orders
- ✅ **Recent Orders**: Get recent orders with limit
- ✅ **Pending Orders**: Get all pending orders
- ✅ **Failed Orders**: Get all failed orders

## API Endpoints

### Core CRUD Operations
```
POST   /api/orders                     - Place new order
GET    /api/orders/{orderId}           - Get order by ID
GET    /api/orders/order-number/{num}  - Get order by order number
GET    /api/orders                     - Get all orders
GET    /api/orders/user/{userId}       - Get orders by user ID
PUT    /api/orders/{orderId}           - Update order
DELETE /api/orders/{orderId}           - Delete order
```

### Order Status Management
```
PATCH  /api/orders/{orderId}/confirm   - Confirm order
PATCH  /api/orders/{orderId}/process   - Start processing
PATCH  /api/orders/{orderId}/ship      - Mark as shipped
PATCH  /api/orders/{orderId}/deliver   - Mark as delivered
PATCH  /api/orders/{orderId}/cancel    - Cancel order
PATCH  /api/orders/{orderId}/refund    - Refund order
PATCH  /api/orders/{orderId}/return    - Return order
```

### Payment Management
```
PATCH  /api/orders/{orderId}/payment/process   - Process payment
PATCH  /api/orders/{orderId}/payment/complete  - Complete payment
PATCH  /api/orders/{orderId}/payment/failed    - Mark payment failed
PATCH  /api/orders/{orderId}/payment/refund    - Refund payment
```

### Search & Filter
```
GET    /api/orders/status/{status}              - Filter by status
GET    /api/orders/payment-status/{payStatus}   - Filter by payment status
GET    /api/orders/date-range?start=&end=       - Filter by date range
GET    /api/orders/search/customer?searchTerm=  - Search by customer
```

### Business Operations
```
GET    /api/orders/{orderId}/total        - Calculate order total
GET    /api/orders/{orderId}/can-cancel   - Check if can cancel
GET    /api/orders/{orderId}/can-ship     - Check if can ship
GET    /api/orders/{orderId}/can-refund   - Check if can refund
```

### Analytics
```
GET    /api/orders/analytics/count     - Total order count
GET    /api/orders/analytics/revenue   - Total revenue
GET    /api/orders/recent?limit=10     - Recent orders
GET    /api/orders/pending             - Pending orders
GET    /api/orders/failed              - Failed orders
```

## Data Models

### Order Entity
- **Basic Info**: ID, Order Number, User ID
- **Status**: OrderStatus, PaymentStatus
- **Financial**: Subtotal, Tax, Shipping, Discount, Total
- **Addresses**: Embedded Shipping & Billing addresses
- **Items**: One-to-Many OrderItems
- **Payment**: Method, Transaction ID, Payment Date
- **Shipping**: Method, Tracking Number, Dates
- **Notes**: Customer Notes, Admin Notes
- **Timestamps**: Created, Updated, Cancelled, Completed

### OrderItem Entity
- **Product Info**: Product ID, SKU, Name, Description
- **Pricing**: Unit Price, Quantity, Discount, Total
- **Details**: Category, Brand, Image URL, Tax Rate

### Embedded Address Classes
- **ShippingAddress**: Complete shipping information
- **BillingAddress**: Billing details (mapped to different columns)

### DTOs with Validation
- **OrderRequest**: Comprehensive validation for order creation
- **OrderUpdateRequest**: Validation for order updates
- **OrderItemRequest**: Validation for order items
- **ShippingAddressRequest**: Address validation
- **BillingAddressRequest**: Billing address validation

## Status Enums

### OrderStatus
- PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED, REFUNDED, RETURNED, FAILED

### PaymentStatus  
- PENDING, PROCESSING, COMPLETED, FAILED, REFUNDED, PARTIALLY_REFUNDED, CANCELLED

## Key Features

### ✅ **Validation System**
- Bean Validation annotations throughout DTOs
- Business logic validation in service layer
- Input validation in controller layer

### ✅ **Inventory Integration**
- Real-time stock checking before order creation
- Circuit breaker and retry patterns for resilience
- Fallback methods for service failures

### ✅ **Event Driven Architecture**
- Kafka integration for order events
- OrderPlacedEvent published on successful order creation

### ✅ **Comprehensive Error Handling**
- Detailed error messages and logging
- Proper HTTP status codes
- Exception handling throughout the stack

### ✅ **Advanced Repository Queries**
- Custom query methods for complex searches
- Date range filtering
- Status-based filtering
- Customer search functionality

### ✅ **Business Rules Enforcement**
- Order state transition validation
- Payment prerequisite checks
- Cancellation and refund eligibility

## Database Design

### Embedded Addresses
- Uses `@Embedded` and `@AttributeOverrides` for performance
- Single table design reduces joins
- Separate column mapping for billing vs shipping addresses

### UUID Primary Keys
- Uses UUID for better distributed system support
- Repository updated to use `UUID` instead of `Long`

### Audit Trails
- Creation and update timestamps
- Status change tracking through admin notes
- Complete order lifecycle tracking

## Testing & Quality

### ✅ **Compilation Verified**
- All code compiles successfully
- No missing dependencies
- Proper import statements

### ✅ **Production Ready**
- Comprehensive logging
- Error handling
- Input validation
- Status management

This order management system now provides enterprise-level functionality for e-commerce operations with full order lifecycle management, payment processing, inventory integration, and comprehensive reporting capabilities.
