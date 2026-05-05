package com.kien.keycoffee.mapper;

import com.kien.keycoffee.dto.CustomerInfoDTO;
import com.kien.keycoffee.dto.CustomerTableDTO;
import com.kien.keycoffee.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CustomerMapper {

    CustomerInfoDTO toCustomerInfoDTO(Customer customer);

    @Mapping(target = "phone", source = "phone")
    CustomerTableDTO toCustomerTableDTO(Customer customer);
}
