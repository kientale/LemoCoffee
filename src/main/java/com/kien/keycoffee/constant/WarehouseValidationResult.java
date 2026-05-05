package com.kien.keycoffee.constant;

import lombok.Getter;

@Getter
public enum WarehouseValidationResult {

    VALID("Validation passed"),
    INVALID_ID("Invalid ingredient ID"),
    INVALID_NAME("Ingredient name must be 2-100 characters and contain only letters, numbers, and single spaces."),
    INVALID_QUANTITY("Quantity must be a positive number."),
    INVALID_UNIT("Unit must be 1-20 characters and contain only letters or numbers."),
    INVALID_SUPPLIER("Supplier must be 2-100 characters and contain only letters, numbers, and single spaces."),
    INVALID_DESCRIPTION("Description is too long."),
    INVALID_STATUS("Invalid ingredient status."),
    NAME_EXISTS("Ingredient name already exists");

    private final String message;

    WarehouseValidationResult(String message) {
        this.message = message;
    }
}
