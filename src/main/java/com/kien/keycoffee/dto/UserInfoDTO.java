package com.kien.keycoffee.dto;

import com.kien.keycoffee.constant.AccountRoleEnum;
import com.kien.keycoffee.constant.AccountStatusEnum;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfoDTO {

    private Integer userId;
    private Integer accountId;

    private String username;
    private String fullName;

    private String password;
    private String confirmPassword;

    private String email;
    private String phone;

    private AccountRoleEnum role;
    private AccountStatusEnum status;

    private LocalDateTime createdAt;
}