package com.kien.lemocoffee.validate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kien.lemocoffee.constant.DrinkStatusEnum;
import com.kien.lemocoffee.constant.DrinkValidationResult;
import com.kien.lemocoffee.dto.DrinkInfoDTO;
import com.kien.lemocoffee.dto.SelectedIngredientDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

        validateName(formData.getName(), errors);
        validatePrice(formData.getPrice(), errors);
        validateDescription(formData.getDescription(), errors);
        validateImage(imageFile, true, errors);
        validateSelectedIngredients(formData.getSelectedIngredientsJson(), errors);

        return errors;
    }

    public List<String> validateForUpdate(DrinkInfoDTO formData) {
        return validateForUpdate(formData, null);
    }

    public List<String> validateForUpdate(DrinkInfoDTO formData, MultipartFile imageFile) {
        List<String> errors = new ArrayList<>();

        Integer id = formData.getId();

        if (id == null || id <= 0) {
            errors.add(DrinkValidationResult.INVALID_ID.getMessage());
            return errors;
        }

        validateName(formData.getName(), errors);
        validatePrice(formData.getPrice(), errors);
        validateDescription(formData.getDescription(), errors);
        validateImage(imageFile, false, errors);
        validateStatus(formData.getStatus(), errors);
        validateSelectedIngredients(formData.getSelectedIngredientsJson(), errors);

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

        String contentType = trimToEmpty(image.getContentType()).toLowerCase();
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

        List<SelectedIngredientDTO> selectedIngredients;
        try {
            selectedIngredients = objectMapper.readValue(
                    selectedIngredientsJson,
                    new TypeReference<List<SelectedIngredientDTO>>() {
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
        for (SelectedIngredientDTO selectedIngredient : selectedIngredients) {
            Integer ingredientId = selectedIngredient.getId();
            BigDecimal quantity = selectedIngredient.getQuantity();

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

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
