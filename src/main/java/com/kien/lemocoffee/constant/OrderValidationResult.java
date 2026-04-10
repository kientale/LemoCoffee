package com.kien.lemocoffee.constant;

import lombok.Getter;

@Getter
public enum OrderValidationResult {

    VALID("Validation passed"),

    INVALID_ID("Invalid order ID"),
    INVALID_TABLE_ID("Invalid table ID"),
    INVALID_CUSTOMER_ID("Invalid customer ID"),
    INVALID_DRINK_ID("Invalid drink ID"),
    INVALID_ORDER_ITEM_ID("Invalid order item ID"),

    REQUIRED_TABLE("Table is required"),
    REQUIRED_DRINK("At least one drink is required"),
    REQUIRED_QUANTITY("Drink quantity is required"),
    REQUIRED_STATUS("Order status is required"),

    EMPTY_ITEMS("Order must contain at least one drink"),
    DUPLICATE_DRINK("Duplicate drink selected"),

    INVALID_QUANTITY("Drink quantity must be a positive number"),
    INVALID_UNIT_PRICE("Unit price must be a positive number"),
    INVALID_SUBTOTAL("Subtotal must be a positive number"),
    INVALID_TOTAL_AMOUNT("Total amount must be a positive number"),
    INVALID_FINAL_AMOUNT("Final amount must be zero or a positive number"),
    INVALID_DISCOUNT_AMOUNT("Discount amount must be zero or a positive number"),

    INVALID_NOTE("Note is too long"),
    INVALID_STATUS("Invalid order status"),
    INVALID_LOYALTY_ACTION("Invalid loyalty action"),
    INVALID_FREE_DRINK_ID("Invalid free drink ID"),
    INVALID_CUSTOMER_POINTS("Invalid customer points"),

    TABLE_NOT_AVAILABLE("Selected table is not available"),
    DRINK_NOT_AVAILABLE("Selected drink is not available"),
    ORDER_ALREADY_COMPLETED("Completed order cannot be changed"),
    ORDER_ALREADY_CANCELLED("Cancelled order cannot be changed");

    private final String message;

    OrderValidationResult(String message) {
        this.message = message;
    }
}
