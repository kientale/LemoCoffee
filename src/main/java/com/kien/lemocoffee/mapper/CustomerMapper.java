package com.kien.lemocoffee.mapper;

import com.kien.lemocoffee.dto.CustomerInfoDTO;
import com.kien.lemocoffee.dto.CustomerTableDTO;
import com.kien.lemocoffee.entity.Customer;
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
