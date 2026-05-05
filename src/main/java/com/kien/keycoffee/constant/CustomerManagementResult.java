package com.kien.keycoffee.constant;

import lombok.Getter;

@Getter
public enum CustomerManagementResult {

    SUCCESS("Operation completed successfully"),
    UNKNOWN_ERROR("An unexpected error occurred"),
    DATABASE_ERROR("Database error occurred"),
    INVALID_ACTION("Invalid action"),

    CUSTOMER_NOT_FOUND("Customer not found"),
    CUSTOMER_ALREADY_EXISTS("Customer already exists"),
    PHONE_ALREADY_EXISTS("Phone already exists"),

    INVALID_INPUT("Invalid input"),
    VALIDATION_FAILED("Validation failed"),
    MISSING_REQUIRED_FIELDS("Missing required fields"),
    INVALID_PHONE("Invalid phone format"),
    INVALID_STATUS("Invalid status"),

    CREATE_SUCCESS("Customer created successfully"),
    CREATE_FAILED("Failed to create customer"),

    UPDATE_SUCCESS("Customer updated successfully"),
    UPDATE_FAILED("Failed to update customer"),

    DELETE_SUCCESS("Customer deleted successfully"),
    DELETE_FAILED("Failed to delete customer"),

    CHANGE_STATUS_SUCCESS("Customer status updated successfully"),
    CHANGE_STATUS_FAILED("Failed to update customer status");

    private final String message;

    CustomerManagementResult(String message) {
        this.message = message;
    }
}
