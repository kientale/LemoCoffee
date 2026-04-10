package com.kien.lemocoffee.validate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kien.lemocoffee.constant.DrinkStatusEnum;
import com.kien.lemocoffee.constant.DrinkValidationResult;
import com.kien.lemocoffee.dto.DrinkInfoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DrinkValidator {

    private static final BigDecimal MAX_PRICE = new BigDecimal("9999999999.99");
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/png", "image/jpeg");

    private final ObjectMapper objectMapper;

    public List<String> validateForCreate(DrinkInfoDTO formData) {
        return validateForCreate(formData, null);
    }

    public List<String> validateForCreate(DrinkInfoDTO formData, MultipartFile imageFile) {
        List<String> errors = new ArrayList<>();

        String name = normalize(formData.getName());
        BigDecimal price = formData.getPrice();
        String description = normalizeNullable(formData.getDescription());
        String selectedIngredientsJson = normalizeNullable(formData.getSelectedIngredientsJson());

        validateName(name, errors);
        validatePrice(price, errors);
        validateDescription(description, errors);
        validateImage(imageFile, true, errors);
        validateSelectedIngredients(selectedIngredientsJson, errors);

        return errors;
    }

    public List<String> validateForUpdate(DrinkInfoDTO formData) {
        return validateForUpdate(formData, null);
    }

    public List<String> validateForUpdate(DrinkInfoDTO formData, MultipartFile imageFile) {
        List<String> errors = new ArrayList<>();

        Integer id = formData.getId();
        String name = normalize(formData.getName());
        BigDecimal price = formData.getPrice();
        String description = normalizeNullable(formData.getDescription());
        DrinkStatusEnum status = formData.getStatus();
        String selectedIngredientsJson = normalizeNullable(formData.getSelectedIngredientsJson());

        if (id == null || id <= 0) {
            errors.add(DrinkValidationResult.INVALID_ID.getMessage());
            return errors;
        }

        validateName(name, errors);
        validatePrice(price, errors);
        validateDescription(description, errors);
        validateImage(imageFile, false, errors);
        validateStatus(status, errors);
        validateSelectedIngredients(selectedIngredientsJson, errors);

        return errors;
    }

    public List<String> validateForChangeStatus(Integer id, DrinkStatusEnum status) {
        List<String> errors = new ArrayList<>();

        if (id == null || id <= 0) {
            errors.add(DrinkValidationResult.INVALID_ID.getMessage());
        }

        validateStatus(status, errors);

        return errors;
    }

    private void validateName(String name, List<String> errors) {
        if (!StringUtils.hasText(name) || !name.matches("^(?=.{2,100}$)[\\p{L}0-9]+(?: [\\p{L}0-9]+)*$")) {
            errors.add(DrinkValidationResult.INVALID_NAME.getMessage());
        }
    }

    private void validatePrice(BigDecimal price, List<String> errors) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0 || price.compareTo(MAX_PRICE) > 0) {
            errors.add(DrinkValidationResult.INVALID_PRICE.getMessage());
        }
    }

    private void validateDescription(String description, List<String> errors) {
        if (description != null && description.length() > 255) {
            errors.add(DrinkValidationResult.INVALID_DESCRIPTION.getMessage());
        }
    }

    private void validateImage(MultipartFile image, boolean required, List<String> errors) {
        if (image == null || image.isEmpty()) {
            if (required) {
                errors.add(DrinkValidationResult.REQUIRED_IMAGE.getMessage());
            }
            return;
        }

        String contentType = normalize(image.getContentType()).toLowerCase();
        if (!ALLOWED_IMAGE_TYPES.contains(contentType) || image.getSize() > MAX_IMAGE_SIZE) {
            errors.add(DrinkValidationResult.INVALID_IMAGE.getMessage());
        }
    }

    private void validateStatus(DrinkStatusEnum status, List<String> errors) {
        if (status == null || status == DrinkStatusEnum.DELETED) {
            errors.add(DrinkValidationResult.INVALID_STATUS.getMessage());
        }
    }

    private void validateSelectedIngredients(String selectedIngredientsJson, List<String> errors) {
        if (!StringUtils.hasText(selectedIngredientsJson)) {
            errors.add(DrinkValidationResult.INVALID_INGREDIENT.getMessage());
            return;
        }

        List<Map<String, Object>> selectedIngredients;
        try {
            selectedIngredients = objectMapper.readValue(
                    selectedIngredientsJson,
                    new TypeReference<>() {
                    }
            );
        } catch (Exception e) {
            errors.add(DrinkValidationResult.INVALID_INGREDIENT.getMessage());
            return;
        }

        if (selectedIngredients == null || selectedIngredients.isEmpty()) {
            errors.add(DrinkValidationResult.INVALID_INGREDIENT.getMessage());
            return;
        }

        Set<Integer> ingredientIds = new HashSet<>();
        for (Map<String, Object> item : selectedIngredients) {
            Integer ingredientId = toInteger(item.get("id"));
            BigDecimal quantity = toBigDecimal(item.get("quantity"));

            if (ingredientId == null || ingredientId <= 0) {
                errors.add(DrinkValidationResult.INVALID_INGREDIENT.getMessage());
                return;
            }

            if (!ingredientIds.add(ingredientId)) {
                errors.add(DrinkValidationResult.DUPLICATE_INGREDIENT.getMessage());
                return;
            }

            if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
                errors.add(DrinkValidationResult.INVALID_INGREDIENT_QUANTITY.getMessage());
                return;
            }
        }
    }

    private Integer toInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }

        try {
            return Integer.valueOf(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }

        try {
            return new BigDecimal(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    private String normalizeNullable(String value) {
        String normalized = normalize(value);
        return normalized.isEmpty() ? null : normalized;
    }
}
