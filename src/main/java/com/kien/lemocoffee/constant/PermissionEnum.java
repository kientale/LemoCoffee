package com.kien.lemocoffee.constant;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public enum PermissionEnum {
    USER_VIEW,
    USER_CREATE,
    USER_UPDATE,
    USER_DELETE,
    USER_RESET_PASSWORD,
    USER_ROLE_UPDATE,
    USER_STATUS_UPDATE,

    INGREDIENT_VIEW,
    INGREDIENT_CREATE,
    INGREDIENT_UPDATE,
    INGREDIENT_DELETE,

    DRINK_VIEW,
    DRINK_CREATE,
    DRINK_UPDATE,
    DRINK_DELETE,
    DRINK_STATUS_UPDATE,

    ORDER_VIEW,
    ORDER_CREATE,
    ORDER_UPDATE,

    TABLE_VIEW,
    TABLE_UPDATE,

    STATISTIC_VIEW;

    public String getAuthority() {
        return name();
    }

    public GrantedAuthority asAuthority() {
        return new SimpleGrantedAuthority(getAuthority());
    }
}
