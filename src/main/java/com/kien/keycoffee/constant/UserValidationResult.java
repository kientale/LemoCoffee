package com.kien.keycoffee.constant;

import lombok.Getter;

@Getter
public enum UserValidationResult {

    VALID("Validation passed"),
    INVALID_ID("Invalid user ID"),
    INVALID_USERNAME("Username must be 4–50 characters and contain only letters, numbers, or underscore."),
    INVALID_PASSWORD("Password must be at least 6 characters and contain both letters and numbers."),
    PASSWORD_NOT_MATCH("Password confirmation does not match"),
    INVALID_FULLNAME("Full name must contain only letters and spaces."),
    INVALID_EMAIL("Email format is invalid."),
    INVALID_PHONE("Phone number must be 9–11 digits."),
    INVALID_ROLE("Invalid role selected."),
    INVALID_STATUS("Invalid user status."),
    USERNAME_EXISTS("Username already exists"),
    EMAIL_EXISTS("Email already exists"),
    PHONE_EXISTS("Phone number already exists");

    private final String message;

    UserValidationResult(String message) {
        this.message = message;
    }

}
