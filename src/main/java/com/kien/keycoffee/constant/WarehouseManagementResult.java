package com.kien.keycoffee.constant;

import lombok.Getter;

@Getter
public enum WarehouseManagementResult {

    SUCCESS("Operation completed successfully"),
    UNKNOWN_ERROR("An unexpected error occurred"),
    DATABASE_ERROR("Database error occurred"),
    INVALID_ACTION("Invalid action"),

    INGREDIENT_NOT_FOUND("Ingredient not found"),
    INGREDIENT_ALREADY_EXISTS("Ingredient already exists"),

    INVALID_INPUT("Invalid input"),
    VALIDATION_FAILED("Validation failed"),
    MISSING_REQUIRED_FIELDS("Missing required fields"),
    INVALID_NAME("Invalid ingredient name"),
    INVALID_QUANTITY("Invalid quantity"),
    INVALID_UNIT("Invalid unit"),
    INVALID_SUPPLIER("Invalid supplier"),
    INVALID_STATUS("Invalid status"),

    CREATE_SUCCESS("Ingredient created successfully"),
    CREATE_FAILED("Failed to create ingredient"),

    UPDATE_SUCCESS("Ingredient updated successfully"),
    UPDATE_FAILED("Failed to update ingredient"),

    DELETE_SUCCESS("Ingredient deleted successfully"),
    DELETE_FAILED("Failed to delete ingredient"),

    CHANGE_STATUS_SUCCESS("Ingredient status updated successfully"),
    CHANGE_STATUS_FAILED("Failed to update ingredient status");

    private final String message;

    WarehouseManagementResult(String message) {
        this.message = message;
    }
}
