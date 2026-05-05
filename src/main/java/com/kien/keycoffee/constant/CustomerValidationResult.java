package com.kien.keycoffee.constant;

import lombok.Getter;

@Getter
public enum CustomerValidationResult {

    VALID("Validation passed"),
    INVALID_ID("Invalid customer ID"),
    INVALID_FULLNAME("Full name must contain only letters and spaces."),
    INVALID_PHONE("Phone number must be 9–11 digits."),
    INVALID_STATUS("Invalid user status."),
    PHONE_EXISTS("Phone number already exists");

    private final String message;

    CustomerValidationResult(String message) {
        this.message = message;
    }
}
