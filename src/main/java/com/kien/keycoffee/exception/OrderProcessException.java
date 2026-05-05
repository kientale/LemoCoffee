package com.kien.keycoffee.exception;

import com.kien.keycoffee.constant.OrderManagementResult;
import lombok.Getter;

@Getter
public class OrderProcessException extends RuntimeException {

    private final OrderManagementResult result;

    public OrderProcessException(OrderManagementResult result) {
        super(result == null ? "Order processing failed" : result.getMessage());
        this.result = result == null ? OrderManagementResult.UNKNOWN_ERROR : result;
    }
}
