package com.kien.lemocoffee.mapper;

import com.kien.lemocoffee.dto.UserInfoDTO;
import com.kien.lemocoffee.dto.UserTableDTO;
import com.kien.lemocoffee.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserMapper {

    @Mapping(target = "userId", source = "id")
    @Mapping(target = "accountId", source = "account.id")
    @Mapping(target = "username", source = "account.username")
    @Mapping(target = "role", source = "account.role")
    @Mapping(target = "status", source = "account.status")
    UserTableDTO toUserTableDTO(User user);

    @Mapping(target = "userId", source = "id")
    @Mapping(target = "accountId", source = "account.id")
    @Mapping(target = "username", source = "account.username")
    @Mapping(target = "role", source = "account.role")
    @Mapping(target = "status", source = "account.status")
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "confirmPassword", ignore = true)
    @Mapping(target = "createdAt", source = "account.createdAt")
    UserInfoDTO toUserInfoDTO(User user);
}