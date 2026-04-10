package com.kien.lemocoffee.dto;

import com.kien.lemocoffee.constant.AccountRoleEnum;
import com.kien.lemocoffee.constant.AccountStatusEnum;
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