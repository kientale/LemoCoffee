package com.kien.keycoffee.normalizer;

import com.kien.keycoffee.dto.CustomerInfoDTO;
import org.springframework.stereotype.Component;

@Component
public class CustomerInfoNormalizer {

    public CustomerInfoDTO normalize(CustomerInfoDTO formData) {
        if (formData == null) {
            return null;
        }

        formData.setFullName(normalizeFullName(formData.getFullName()));
        formData.setPhone(normalizePhone(formData.getPhone()));

        return formData;
    }

    public String normalizeFullName(String value) {
        return trimToEmpty(value).replaceAll("\\s+", " ");
    }

    public String normalizePhone(String value) {
        return trimToEmpty(value);
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
