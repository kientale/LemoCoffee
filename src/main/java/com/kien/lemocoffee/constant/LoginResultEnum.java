package com.kien.lemocoffee.constant;

import lombok.Getter;

@Getter
public enum LoginResultEnum {

    USER_NOT_FOUND("Account does not exist."),
    WRONG_PASSWORD_OR_USERNAME("Incorrect password or username."),
    ACCOUNT_LOCKED("Your account has been locked.");

    private final String message;

    LoginResultEnum(String message) {
        this.message = message;
    }
}
