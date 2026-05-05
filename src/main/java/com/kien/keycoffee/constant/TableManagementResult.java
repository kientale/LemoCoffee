package com.kien.keycoffee.constant;

import lombok.Getter;

@Getter
public enum TableManagementResult {
    SUCCESS("Operation completed successfully"),
    TABLE_NOT_FOUND("Table not found"),

    CREATE_SUCCESS("Table created successfully"),
    CREATE_FAILED("Failed to create table"),

    UPDATE_SUCCESS("Table updated successfully"),
    UPDATE_FAILED("Failed to update table"),

    CHANGE_STATUS_SUCCESS("Table status updated successfully"),
    CHANGE_STATUS_FAILED("Failed to update table status"),

    DELETE_SUCCESS("Table deleted successfully"),
    DELETE_FAILED("Failed to delete table");

    private final String message;

    TableManagementResult(String message) {
        this.message = message;
    }
}
