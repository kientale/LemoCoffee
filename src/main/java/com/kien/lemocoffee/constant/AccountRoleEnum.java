package com.kien.lemocoffee.constant;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Getter
public enum AccountRoleEnum {
    ADMIN(allPermissions()),
    STAFF(staffPermissions());

    private final Set<PermissionEnum> permissions;

    AccountRoleEnum(Set<PermissionEnum> permissions) {
        this.permissions = permissions;
    }

    public List<GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>(permissions.size() + 1);

        for (PermissionEnum permission : permissions) {
            authorities.add(permission.asAuthority());
        }

        authorities.add(new SimpleGrantedAuthority("ROLE_" + name()));
        return List.copyOf(authorities);
    }

    private static Set<PermissionEnum> staffPermissions() {
        return Collections.unmodifiableSet(EnumSet.of(
                PermissionEnum.USER_VIEW,
                PermissionEnum.USER_UPDATE,
                PermissionEnum.INGREDIENT_VIEW,
                PermissionEnum.DRINK_VIEW,
                PermissionEnum.ORDER_VIEW,
                PermissionEnum.ORDER_CREATE,
                PermissionEnum.ORDER_UPDATE,
                PermissionEnum.TABLE_VIEW,
                PermissionEnum.TABLE_UPDATE,
                PermissionEnum.STATISTIC_VIEW
        ));
    }

    private static Set<PermissionEnum> allPermissions() {
        return Collections.unmodifiableSet(EnumSet.allOf(PermissionEnum.class));
    }
}