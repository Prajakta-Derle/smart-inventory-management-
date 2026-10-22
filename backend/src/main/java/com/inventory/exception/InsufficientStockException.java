package com.inventory.exception;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String productName, int currentStock, int requested) {
        super("Insufficient stock for '" + productName + "': current=" + currentStock
              + ", requested=" + requested + ". Resulting stock would be negative.");
    }
}
