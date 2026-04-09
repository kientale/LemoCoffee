package com.kien.lemocoffee.feature.user.utils;

import com.kien.lemocoffee.feature.auth.entity.enums.AccountRoleEnum;
import com.kien.lemocoffee.feature.auth.entity.enums.AccountStatusEnum;
import com.kien.lemocoffee.feature.auth.repository.AccountRepository;
import com.kien.lemocoffee.feature.user.dto.UserInfoDTO;
import com.kien.lemocoffee.feature.user.entity.enums.UserValidationResult;
import com.kien.lemocoffee.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class UserValidator {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public List<String> validateForCreate(UserInfoDTO formData) {
        List<String> errors = new ArrayList<>();

        String username = normalize(formData.getUsername());
        String fullName = normalize(formData.getFullName());
        String password = normalize(formData.getPassword());
        String confirmPassword = normalize(formData.getConfirmPassword());
        String email = normalizeNullable(formData.getEmail());
        String phone = normalizeNullable(formData.getPhone());
        AccountRoleEnum role = formData.getRole();

        validateUsername(username, null, errors);
        validateFullName(fullName, errors);
        validatePassword(password, confirmPassword, errors);
        validateEmail(email, null, errors);
        validatePhone(phone, null, errors);
        validateRole(role, errors);

        return errors;
    }

    public List<String> validateForUpdate(UserInfoDTO formData) {
        List<String> errors = new ArrayList<>();

        Integer accountId = formData.getAccountId();
        String username = normalize(formData.getUsername());
        String fullName = normalize(formData.getFullName());
        String email = normalizeNullable(formData.getEmail());
        String phone = normalizeNullable(formData.getPhone());
        AccountRoleEnum role = formData.getRole();
        AccountStatusEnum status = formData.getStatus();

        if (accountId == null || accountId <= 0) {
            errors.add(UserValidationResult.INVALID_ID.getMessage());
            return errors;
        }

        validateUsername(username, accountId, errors);
        validateFullName(fullName, errors);
        validateEmail(email, accountId, errors);
        validatePhone(phone, accountId, errors);
        validateRole(role, errors);
        validateStatus(status, errors);

        return errors;
    }

    private void validateUsername(String username, Integer accountId, List<String> errors) {
        if (!StringUtils.hasText(username) || !username.matches("^[A-Za-z0-9_]{4,50}$")) {
            errors.add(UserValidationResult.INVALID_USERNAME.getMessage());
            return;
        }

        boolean exists = (accountId == null)
                ? accountRepository.existsByUsernameIgnoreCase(username)
                : accountRepository.existsByUsernameIgnoreCaseAndIdNot(username, accountId);

        if (exists) {
            errors.add(UserValidationResult.USERNAME_EXISTS.getMessage());
        }
    }

    private void validateFullName(String fullName, List<String> errors) {
        if (!StringUtils.hasText(fullName) || !fullName.matches("^[\\p{L} ]{2,100}$")) {
            errors.add(UserValidationResult.INVALID_FULLNAME.getMessage());
        }
    }

    private void validatePassword(String password, String confirmPassword, List<String> errors) {
        if (!StringUtils.hasText(password) || !password.matches("^(?=.*[A-Za-z])(?=.*\\d).{6,}$")) {
            errors.add(UserValidationResult.INVALID_PASSWORD.getMessage());
        }

        if (!Objects.equals(password, confirmPassword)) {
            errors.add(UserValidationResult.PASSWORD_NOT_MATCH.getMessage());
        }
    }

    private void validateEmail(String email, Integer accountId, List<String> errors) {
        if (!StringUtils.hasText(email)) {
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            errors.add(UserValidationResult.INVALID_EMAIL.getMessage());
            return;
        }

        boolean exists = (accountId == null)
                ? userRepository.existsByEmailIgnoreCase(email)
                : userRepository.existsByEmailIgnoreCaseAndAccount_IdNot(email, accountId);

        if (exists) {
            errors.add(UserValidationResult.EMAIL_EXISTS.getMessage());
        }
    }

    private void validatePhone(String phone, Integer accountId, List<String> errors) {
        if (!StringUtils.hasText(phone)) {
            return;
        }

        if (!phone.matches("^\\d{9,11}$")) {
            errors.add(UserValidationResult.INVALID_PHONE.getMessage());
            return;
        }

        boolean exists = (accountId == null)
                ? userRepository.existsByPhone(phone)
                : userRepository.existsByPhoneAndAccount_IdNot(phone, accountId);

        if (exists) {
            errors.add(UserValidationResult.PHONE_EXISTS.getMessage());
        }
    }

    private void validateRole(AccountRoleEnum role, List<String> errors) {
        if (role == null) {
            errors.add(UserValidationResult.INVALID_ROLE.getMessage());
        }
    }

    private void validateStatus(AccountStatusEnum status, List<String> errors) {
        if (status == null) {
            errors.add(UserValidationResult.INVALID_STATUS.getMessage());
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeNullable(String value) {
        String normalized = normalize(value);
        return normalized.isEmpty() ? null : normalized;
    }
}