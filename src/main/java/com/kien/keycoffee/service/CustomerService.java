package com.kien.keycoffee.service;

import com.kien.keycoffee.dto.CustomerInfoDTO;
import com.kien.keycoffee.dto.CustomerTableDTO;
import com.kien.keycoffee.constant.CustomerManagementResult;
import com.kien.keycoffee.constant.CustomerStatusEnum;
import org.springframework.data.domain.Page;

public interface CustomerService {

    Page<CustomerTableDTO> getCustomer(int page, int size, String keyword);

    CustomerManagementResult createCustomer(CustomerInfoDTO formData);

    CustomerInfoDTO getCustomerInfoById(Integer id);

    CustomerInfoDTO getCustomerInfoByPhone(String phone);

    CustomerManagementResult updateCustomer(CustomerInfoDTO formData);

    CustomerManagementResult deleteCustomer(Integer id, CustomerStatusEnum status);
}
