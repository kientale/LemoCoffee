package com.kien.lemocoffee.validate;

import com.kien.lemocoffee.dto.CustomerInfoDTO;
import com.kien.lemocoffee.constant.CustomerStatusEnum;
import com.kien.lemocoffee.constant.CustomerValidationResult;
import com.kien.lemocoffee.repository.CustomerRepository;
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

        String fullName = normalize(formData.getFullName());
        String phone = normalize(formData.getPhone());

        validateFullName(fullName, errors);
        validatePhone(phone, null, errors);

        return errors;
    }

    public List<String> validateForUpdate(CustomerInfoDTO formData) {
        List<String> errors = new ArrayList<>();

        Integer id = formData.getId();
        String fullName = normalize(formData.getFullName());
        String phone = normalize(formData.getPhone());
        CustomerStatusEnum status = formData.getStatus();

        if (id == null || id <= 0) {
            errors.add(CustomerValidationResult.INVALID_ID.getMessage());
            return errors;
        }

        validateFullName(fullName, errors);
        validatePhone(phone, id, errors);
        validateStatus(status, errors);

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

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ");
    }
}
