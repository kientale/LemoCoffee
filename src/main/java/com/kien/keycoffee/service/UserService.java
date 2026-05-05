package com.kien.keycoffee.service;

import com.kien.keycoffee.constant.AccountStatusEnum;
import com.kien.keycoffee.dto.UserInfoDTO;
import com.kien.keycoffee.dto.UserTableDTO;
import com.kien.keycoffee.constant.UserManagementResult;
import org.springframework.data.domain.Page;

public interface UserService {

    Page<UserTableDTO> getUser(int page, int size, String keyword, String field);

    UserManagementResult createUser(UserInfoDTO formData);

    UserInfoDTO getUserInfoByAccountId(Integer accountId);

    UserManagementResult updateUser(UserInfoDTO formData);

    UserManagementResult deleteUser(Integer accountId);

    UserManagementResult changeUserStatus(Integer accountId, AccountStatusEnum status);

    UserManagementResult resetPassword(int accountId);
}