package com.kien.keycoffee.constant;

import lombok.Getter;

@Getter
public enum TableValidateResult {
    VALID("Validation passed"),
    INVALID_ID("Invalid table ID"),
    INVALID_CAPACITY("Capacity must be between 1 and 20"),
    INVALID_STATUS("Invalid table status");

    private final String message;

    TableValidateResult(String message) {
        this.message = message;
    }
}
