package com.kien.lemocoffee.constant;

import lombok.Getter;

@Getter
public enum DrinkValidationResult {

    VALID("Validation passed"),
    INVALID_ID("Invalid drink ID"),
    INVALID_NAME("Drink name must be 2-100 characters and contain only letters, numbers, and single spaces."),
    INVALID_PRICE("Price must be a positive number."),
    INVALID_DESCRIPTION("Description is too long."),
    INVALID_IMAGE("Image must be PNG, JPG, or JPEG and must not exceed 5MB."),
    REQUIRED_IMAGE("Drink image is required."),
    INVALID_STATUS("Invalid drink status."),
    INVALID_INGREDIENT("A drink must contain at least one valid ingredient."),
    INVALID_INGREDIENT_QUANTITY("Ingredient quantity must be a positive number."),
    DUPLICATE_INGREDIENT("Duplicate ingredient selected."),
    NAME_EXISTS("Drink name already exists");

    private final String message;

    DrinkValidationResult(String message) {
        this.message = message;
    }
}
