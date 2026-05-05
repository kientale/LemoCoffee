package com.kien.keycoffee.validate;

import com.kien.keycoffee.dto.CustomerInfoDTO;
import com.kien.keycoffee.constant.CustomerStatusEnum;
import com.kien.keycoffee.constant.CustomerValidationResult;
import com.kien.keycoffee.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CustomerValidator {

    private final CustomerRepository customerRepository;

    public List<String> validateForCreate(CustomerInfoDTO formData) {
        List<String> errors = new ArrayList<>();

        validateFullName(formData.getFullName(), errors);
        validatePhone(formData.getPhone(), null, errors);

        return errors;
    }

    public List<String> validateForUpdate(CustomerInfoDTO formData) {
        List<String> errors = new ArrayList<>();

        Integer id = formData.getId();

        if (id == null || id <= 0) {
            errors.add(CustomerValidationResult.INVALID_ID.getMessage());
            return errors;
        }

        validateFullName(formData.getFullName(), errors);
        validatePhone(formData.getPhone(), id, errors);
        validateStatus(formData.getStatus(), errors);

        return errors;
    }

    private void validateFullName(String fullName, List<String> errors) {
        if (!StringUtils.hasText(fullName) || !fullName.matches("^[\\p{L}\\p{M} ]{2,100}$")) {
            errors.add(CustomerValidationResult.INVALID_FULLNAME.getMessage());
        }
    }

    private void validatePhone(String phone, Integer customerId, List<String> errors) {
        if (!StringUtils.hasText(phone) || !phone.matches("^\\d{9,11}$")) {
            errors.add(CustomerValidationResult.INVALID_PHONE.getMessage());
            return;
        }

        boolean exists = (customerId == null)
                ? customerRepository.existsByPhone(phone)
                : customerRepository.existsByPhoneAndIdNot(phone, customerId);

        if (exists) {
            errors.add(CustomerValidationResult.PHONE_EXISTS.getMessage());
        }
    }

    private void validateStatus(CustomerStatusEnum status, List<String> errors) {
        if (status == null) {
            errors.add(CustomerValidationResult.INVALID_STATUS.getMessage());
        }
    }
}
