package com.kien.keycoffee.constant;

import lombok.Getter;

@Getter
public enum UserManagementResult {

    SUCCESS("Operation completed successfully"),
    UNKNOWN_ERROR("An unexpected error occurred"),
    DATABASE_ERROR("Database error occurred"),
    INVALID_ACTION("Invalid action"),

    USER_NOT_FOUND("User not found"),
    ACCOUNT_NOT_FOUND("Account not found"),
    USER_ALREADY_EXISTS("User already exists"),
    USERNAME_ALREADY_EXISTS("Username already exists"),
    EMAIL_ALREADY_EXISTS("Email already exists"),
    PHONE_ALREADY_EXISTS("Phone already exists"),

    INVALID_INPUT("Invalid input"),
    VALIDATION_FAILED("Validation failed"),
    MISSING_REQUIRED_FIELDS("Missing required fields"),
    INVALID_EMAIL("Invalid email format"),
    INVALID_PHONE("Invalid phone format"),
    INVALID_ROLE("Invalid role"),
    INVALID_STATUS("Invalid status"),
    PASSWORD_MISMATCH("Password confirmation does not match"),
    PASSWORD_INVALID("Password is invalid"),

    CREATE_SUCCESS("User created successfully"),
    CREATE_FAILED("Failed to create user"),

    UPDATE_SUCCESS("User updated successfully"),
    UPDATE_FAILED("Failed to update user"),

    DELETE_SUCCESS("User deleted successfully"),
    DELETE_FAILED("Failed to delete user"),

    RESET_PASSWORD_SUCCESS("Password reset successfully"),
    RESET_PASSWORD_FAILED("Failed to reset password"),
    EMAIL_NOT_FOUND("Email not found"),
    EMAIL_REQUIRED("Email is required"),
    SEND_EMAIL_SUCCESS("Email sent successfully"),
    SEND_EMAIL_FAILED("Failed to send email"),

    CHANGE_STATUS_SUCCESS("User status updated successfully"),
    CHANGE_STATUS_FAILED("Failed to update user status"),
    ACCOUNT_ALREADY_ACTIVE("Account is already active"),
    ACCOUNT_ALREADY_LOCKED("Account is already locked");

    private final String message;

    UserManagementResult(String message) {
        this.message = message;
    }
}