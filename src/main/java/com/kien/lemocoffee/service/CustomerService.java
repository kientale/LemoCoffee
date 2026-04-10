package com.kien.lemocoffee.service;

import com.kien.lemocoffee.dto.CustomerInfoDTO;
import com.kien.lemocoffee.dto.CustomerTableDTO;
import com.kien.lemocoffee.constant.CustomerManagementResult;
import com.kien.lemocoffee.constant.CustomerStatusEnum;
import org.springframework.data.domain.Page;

public interface CustomerService {

    Page<CustomerTableDTO> getCustomer(int page, int size, String keyword);

    CustomerManagementResult createCustomer(CustomerInfoDTO formData);

    CustomerInfoDTO getCustomerInfoById(Integer id);

    CustomerInfoDTO getCustomerInfoByPhone(String phone);

    CustomerManagementResult updateCustomer(CustomerInfoDTO formData);

    CustomerManagementResult deleteCustomer(Integer id, CustomerStatusEnum status);
}
