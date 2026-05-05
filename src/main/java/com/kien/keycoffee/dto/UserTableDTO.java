package com.kien.keycoffee.dto;

import com.kien.keycoffee.constant.AccountRoleEnum;
import com.kien.keycoffee.constant.AccountStatusEnum;
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
}