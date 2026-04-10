package com.kien.lemocoffee.constant;

import lombok.Getter;

@Getter
public enum DrinkManagementResult {

    SUCCESS("Operation completed successfully"),
    UNKNOWN_ERROR("An unexpected error occurred"),
    DATABASE_ERROR("Database error occurred"),
    INVALID_ACTION("Invalid action"),

    DRINK_NOT_FOUND("Drink not found"),
    DRINK_ALREADY_EXISTS("Drink already exists"),
    INGREDIENT_NOT_FOUND("Ingredient not found"),

    INVALID_INPUT("Invalid input"),
    VALIDATION_FAILED("Validation failed"),
    MISSING_REQUIRED_FIELDS("Missing required fields"),
    INVALID_NAME("Invalid drink name"),
    INVALID_PRICE("Invalid price"),
    INVALID_DESCRIPTION("Invalid description"),
    INVALID_IMAGE("Invalid image"),
    IMAGE_SAVE_FAILED("Failed to save drink image"),
    INVALID_STATUS("Invalid status"),
    INVALID_INGREDIENT("Invalid ingredient"),
    INVALID_INGREDIENT_QUANTITY("Invalid ingredient quantity"),

    CREATE_SUCCESS("Drink created successfully"),
    CREATE_FAILED("Failed to create drink"),

    UPDATE_SUCCESS("Drink updated successfully"),
    UPDATE_FAILED("Failed to update drink"),

    DELETE_SUCCESS("Drink deleted successfully"),
    DELETE_FAILED("Failed to delete drink"),

    CHANGE_STATUS_SUCCESS("Drink status updated successfully"),
    CHANGE_STATUS_FAILED("Failed to update drink status");

    private final String message;

    DrinkManagementResult(String message) {
        this.message = message;
    }
}
