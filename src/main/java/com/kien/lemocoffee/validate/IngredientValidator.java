package com.kien.lemocoffee.validate;

import com.kien.lemocoffee.constant.IngredientStatusEnum;
import com.kien.lemocoffee.constant.WarehouseValidationResult;
import com.kien.lemocoffee.dto.IngredientInfoDTO;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class IngredientValidator {

    public List<String> validateForCreate(IngredientInfoDTO formData) {
        List<String> errors = new ArrayList<>();

        validateName(formData.getName(), errors);
        validateQuantity(formData.getQuantity(), errors);
        validateUnit(formData.getUnit(), errors);
        validateSupplier(formData.getSupplier(), errors);
        validateDescription(formData.getDescription(), errors);

        return errors;
    }

    public List<String> validateForUpdate(IngredientInfoDTO formData) {
        List<String> errors = new ArrayList<>();

        Integer id = formData.getId();

        if (id == null || id <= 0) {
            errors.add(WarehouseValidationResult.INVALID_ID.getMessage());
            return errors;
        }

        validateName(formData.getName(), errors);
        validateQuantity(formData.getQuantity(), errors);
        validateUnit(formData.getUnit(), errors);
        validateSupplier(formData.getSupplier(), errors);
        validateDescription(formData.getDescription(), errors);

        return errors;
    }

    public List<String> validateForChangeStatus(Integer id, IngredientStatusEnum status) {
        List<String> errors = new ArrayList<>();

        if (id == null || id <= 0) {
            errors.add(WarehouseValidationResult.INVALID_ID.getMessage());
        }

        if (status == null) {
            errors.add(WarehouseValidationResult.INVALID_STATUS.getMessage());
        }

        return errors;
    }

    private void validateName(String name, List<String> errors) {
        if (!StringUtils.hasText(name) || !name.matches("^(?=.{2,100}$)[\\p{L}0-9]+(?: [\\p{L}0-9]+)*$")) {
            errors.add(WarehouseValidationResult.INVALID_NAME.getMessage());
        }
    }

    private void validateQuantity(BigDecimal quantity, List<String> errors) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            errors.add(WarehouseValidationResult.INVALID_QUANTITY.getMessage());
        }
    }

    private void validateUnit(String unit, List<String> errors) {
        if (!StringUtils.hasText(unit) || !unit.matches("^(?=.{1,20}$)[\\p{L}0-9]+$")) {
            errors.add(WarehouseValidationResult.INVALID_UNIT.getMessage());
        }
    }

    private void validateSupplier(String supplier, List<String> errors) {
        if (!StringUtils.hasText(supplier) || !supplier.matches("^(?=.{2,100}$)[\\p{L}0-9]+(?: [\\p{L}0-9]+)*$")) {
            errors.add(WarehouseValidationResult.INVALID_SUPPLIER.getMessage());
        }
    }

    private void validateDescription(String description, List<String> errors) {
        if (description != null && description.length() > 255) {
            errors.add(WarehouseValidationResult.INVALID_DESCRIPTION.getMessage());
        }
    }
}
