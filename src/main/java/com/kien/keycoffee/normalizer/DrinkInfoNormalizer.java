package com.kien.keycoffee.normalizer;

import com.kien.keycoffee.dto.DrinkInfoDTO;
import org.springframework.stereotype.Component;

@Component
public class DrinkInfoNormalizer {

    public DrinkInfoDTO normalize(DrinkInfoDTO formData) {
        if (formData == null) {
            return null;
        }

        formData.setName(normalizeName(formData.getName()));
        formData.setDescription(normalizeDescription(formData.getDescription()));
        formData.setImage(normalizeImage(formData.getImage()));
        formData.setSelectedIngredientsJson(normalizeSelectedIngredientsJson(formData.getSelectedIngredientsJson()));

        return formData;
    }

    public String normalizeName(String value) {
        return collapseSpaces(value);
    }

    public String normalizeDescription(String value) {
        return trimToNull(value);
    }

    public String normalizeImage(String value) {
        return trimToNull(value);
    }

    public String normalizeSelectedIngredientsJson(String value) {
        return trimToNull(value);
    }

    private String collapseSpaces(String value) {
        return trimToEmpty(value).replaceAll("\\s+", " ");
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String trimToNull(String value) {
        String normalized = trimToEmpty(value);
        return normalized.isEmpty() ? null : normalized;
    }
}
