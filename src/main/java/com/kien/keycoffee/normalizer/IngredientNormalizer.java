package com.kien.keycoffee.normalizer;

import com.kien.keycoffee.dto.IngredientInfoDTO;
import org.springframework.stereotype.Component;

@Component
public class IngredientNormalizer {

    public IngredientInfoDTO normalize(IngredientInfoDTO formData) {
        if (formData == null) {
            return null;
        }

        formData.setName(normalizeName(formData.getName()));
        formData.setDescription(normalizeDescription(formData.getDescription()));
        formData.setUnit(normalizeUnit(formData.getUnit()));
        formData.setSupplier(normalizeSupplier(formData.getSupplier()));

        return formData;
    }

    public String normalizeName(String value) {
        return collapseSpaces(value);
    }

    public String normalizeDescription(String value) {
        return trimToNull(value);
    }

    public String normalizeUnit(String value) {
        return trimToEmpty(value);
    }

    public String normalizeSupplier(String value) {
        return collapseSpaces(value);
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
