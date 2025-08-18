package com.inventory.service.inventory_service.exceptions;

public class ProductNotExist extends RuntimeException {

    public ProductNotExist(String message) {
        super(message);
    }

}
