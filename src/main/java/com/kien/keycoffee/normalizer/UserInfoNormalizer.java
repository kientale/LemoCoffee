package com.kien.keycoffee.normalizer;

import com.kien.keycoffee.dto.UserInfoDTO;
import org.springframework.stereotype.Component;

@Component
public class UserInfoNormalizer {

    public UserInfoDTO normalize(UserInfoDTO formData) {
        if (formData == null) {
            return null;
        }

        formData.setUsername(trimToEmpty(formData.getUsername()));
        formData.setFullName(trimToEmpty(formData.getFullName()));
        formData.setPassword(trimToEmpty(formData.getPassword()));
        formData.setConfirmPassword(trimToEmpty(formData.getConfirmPassword()));
        formData.setEmail(trimToNull(formData.getEmail()));
        formData.setPhone(trimToNull(formData.getPhone()));

        return formData;
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String trimToNull(String value) {
        String normalized = trimToEmpty(value);
        return normalized.isEmpty() ? null : normalized;
    }
}
