package com.kien.lemocoffee.constant;

import lombok.Getter;

@Getter
public enum OrderManagementResult {

    SUCCESS("Operation completed successfully"),
    UNKNOWN_ERROR("An unexpected error occurred"),
    DATABASE_ERROR("Database error occurred"),
    INVALID_ACTION("Invalid action"),
    INVALID_ORDER("Invalid order"),
    DUPLICATE_SELECTED_DRINK("Duplicate selected drink"),

    ORDER_NOT_FOUND("Order not found"),
    TABLE_NOT_FOUND("Table not found"),
    CUSTOMER_NOT_FOUND("Customer not found"),
    DRINK_NOT_FOUND("Drink not found"),
    DRINK_RECIPE_NOT_FOUND("Drink recipe not found"),
    DRINK_RECIPE_INVALID("Drink recipe is invalid"),
    INGREDIENT_NOT_FOUND("Ingredient not found"),
    ORDER_ITEM_NOT_FOUND("Order item not found"),

    INVALID_INPUT("Invalid input"),
    VALIDATION_FAILED("Validation failed"),
    MISSING_REQUIRED_FIELDS("Missing required fields"),
    INVALID_TABLE("Invalid table"),
    INVALID_CUSTOMER("Invalid customer"),
    INVALID_SELECTED_DRINK("Invalid selected drink"),
    INVALID_ORDER_ITEM("Invalid order item"),
    INVALID_QUANTITY("Invalid quantity"),
    INVALID_PRICE("Invalid price"),
    INVALID_TOTAL_AMOUNT("Invalid total amount"),
    INVALID_NOTE("Invalid note"),
    INVALID_STATUS("Invalid order status"),
    EMPTY_ORDER("Order must contain at least one drink"),
    INSUFFICIENT_INGREDIENT_STOCK("Not enough ingredients in stock"),

    TABLE_UNAVAILABLE("Table is not available"),
    DRINK_UNAVAILABLE("Drink is not available"),
    ORDER_ALREADY_COMPLETED("Order is already completed"),
    ORDER_ALREADY_CANCELLED("Order is already cancelled"),
    ORDER_CANNOT_BE_EDITED("Order cannot be edited"),
    ORDER_CANNOT_BE_CANCELLED("Order cannot be cancelled"),
    ORDER_CANNOT_BE_CHECKED_OUT("Order cannot be checked out"),

    CREATE_SUCCESS("Order created successfully"),
    CREATE_FAILED("Failed to create order"),

    UPDATE_SUCCESS("Order updated successfully"),
    UPDATE_FAILED("Failed to update order"),

    CANCEL_SUCCESS("Order cancelled successfully"),
    CANCEL_FAILED("Failed to cancel order"),

    CHECKOUT_SUCCESS("Order checked out successfully"),
    CHECKOUT_FAILED("Failed to check out order"),

    DELETE_SUCCESS("Order deleted successfully"),
    DELETE_FAILED("Failed to delete order"),

    CHANGE_STATUS_SUCCESS("Order status updated successfully"),
    CHANGE_STATUS_FAILED("Failed to update order status"),

    INVOICE_DOWNLOAD_SUCCESS("Invoice downloaded successfully"),
    INVOICE_DOWNLOAD_FAILED("Failed to download invoice"),

    LOYALTY_POINT_UPDATE_SUCCESS("Customer loyalty points updated successfully"),
    LOYALTY_POINT_UPDATE_FAILED("Failed to update customer loyalty points");

    private final String message;

    OrderManagementResult(String message) {
        this.message = message;
    }
}
