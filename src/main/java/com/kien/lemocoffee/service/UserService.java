package com.kien.lemocoffee.service;

import com.kien.lemocoffee.constant.AccountStatusEnum;
import com.kien.lemocoffee.dto.UserInfoDTO;
import com.kien.lemocoffee.dto.UserTableDTO;
import com.kien.lemocoffee.constant.UserManagementResult;
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