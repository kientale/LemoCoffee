package com.kien.lemocoffee.feature.user.dto;

import com.kien.lemocoffee.feature.auth.entity.enums.AccountRoleEnum;
import com.kien.lemocoffee.feature.auth.entity.enums.AccountStatusEnum;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTableDTO {

    private Integer userId;
    private Integer accountId;

    private String fullName;
    private String username;

    private AccountRoleEnum role;
    private AccountStatusEnum status;

    private String email;
    private String phone;
}