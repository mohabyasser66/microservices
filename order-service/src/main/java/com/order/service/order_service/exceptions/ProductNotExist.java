package com.order.service.order_service.exceptions;

public class ProductNotExist extends RuntimeException {

    public ProductNotExist(String message) {
        super(message);
    }

}
