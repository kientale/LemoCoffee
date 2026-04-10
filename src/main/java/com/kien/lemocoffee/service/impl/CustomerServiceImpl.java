package com.kien.lemocoffee.service.impl;

import com.kien.lemocoffee.dto.CustomerInfoDTO;
import com.kien.lemocoffee.dto.CustomerTableDTO;
import com.kien.lemocoffee.entity.Customer;
import com.kien.lemocoffee.constant.CustomerManagementResult;
import com.kien.lemocoffee.constant.CustomerStatusEnum;
import com.kien.lemocoffee.mapper.CustomerMapper;
import com.kien.lemocoffee.repository.CustomerRepository;
import com.kien.lemocoffee.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    public Page<CustomerTableDTO> getCustomer(int page, int size, String keyword) {
        int pageNo = Math.max(1, page);
        int pageSize = Math.max(1, size);

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, Sort.by(Sort.Direction.DESC, "id"));

        String kw = normalize(keyword);

        Page<Customer> customerPage;

        if (kw.isEmpty()) {
            customerPage = customerRepository.findByStatusNot(CustomerStatusEnum.DELETED, pageable);
        } else {
            customerPage = customerRepository.findByFullNameContainingIgnoreCaseAndStatusNot(
                    kw,
                    CustomerStatusEnum.DELETED,
                    pageable
            );
        }

        return customerPage.map(customerMapper::toCustomerTableDTO);
    }

    @Override
    @Transactional
    public CustomerManagementResult createCustomer(CustomerInfoDTO formData) {
        try {
            String fullName = normalize(formData.getFullName());
            String phone = normalize(formData.getPhone());

            Customer customer = Customer.builder()
                    .fullName(fullName)
                    .phone(phone)
                    .status(CustomerStatusEnum.ACTIVE)
                    .points(0)
                    .build();

            customerRepository.save(customer);

            return CustomerManagementResult.CREATE_SUCCESS;
        } catch (Exception e) {
            log.error("Failed to create customer with fullname={}, phone={}",
                    formData.getFullName(),
                    formData.getPhone(),
                    e);
            return CustomerManagementResult.CREATE_FAILED;
        }
    }

    @Override
    @Transactional
    public CustomerManagementResult updateCustomer(CustomerInfoDTO formData) {
        try {
            Integer id = formData.getId();
            Customer customer = findCustomerById(id);
            if (customer == null) {
                return  CustomerManagementResult.CUSTOMER_NOT_FOUND;
            }

            customer.setFullName(formData.getFullName());
            customer.setPhone(formData.getPhone());
            customer.setStatus(formData.getStatus());

            customerRepository.save(customer);

            return CustomerManagementResult.UPDATE_SUCCESS;
        } catch (Exception e) {
            log.error("Failed to update customer with id={}",
                    formData.getId(),
                    e);
            return CustomerManagementResult.UPDATE_FAILED;
        }
    }

    @Override
    @Transactional
    public CustomerManagementResult deleteCustomer(Integer id, CustomerStatusEnum status) {
        try {
            if (status == null) {
                return  CustomerManagementResult.DELETE_FAILED;
            }

            Customer customer = findCustomerById(id);
            if (customer == null) {
                return  CustomerManagementResult.CUSTOMER_NOT_FOUND;
            }

            customer.setStatus(status);
            customerRepository.save(customer);
            return CustomerManagementResult.DELETE_SUCCESS;
        } catch (Exception e) {
            log.error("Failed to delete. id={}", id, e);
            return CustomerManagementResult.DELETE_FAILED;
        }
    }

    @Override
    public CustomerInfoDTO getCustomerInfoById(Integer id) {
        return customerRepository.findById(id)
                .map(customerMapper::toCustomerInfoDTO)
                .orElse(null);
    }

    @Override
    public CustomerInfoDTO getCustomerInfoByPhone(String phone) {
        String normalizedPhone = normalize(phone);
        if (normalizedPhone.isEmpty()) {
            return null;
        }

        return customerRepository.findByPhone(normalizedPhone)
                .map(customerMapper::toCustomerInfoDTO)
                .orElse(null);
    }

    private Customer findCustomerById(Integer id) {
        if (isInvalidId(id)) {
            return null;
        }

        Customer customer = customerRepository.findById(id).orElse(null);
        if (customer == null) {
            return null;
        }

        return customer;
    }

    private boolean isInvalidId(Integer id) {
        return id == null || id <= 0;
    }


    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
