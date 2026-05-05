package com.kien.keycoffee.validate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kien.keycoffee.constant.OrderStatusEnum;
import com.kien.keycoffee.constant.OrderValidationResult;
import com.kien.keycoffee.dto.OrderInfoDTO;
import com.kien.keycoffee.dto.SelectedDrinkDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class OrderValidator {

    private static final int MAX_NOTE_LENGTH = 255;
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("9999999999.99");
    private static final Set<String> ALLOWED_LOYALTY_ACTIONS = Set.of("earn", "redeem", "none");

    private final ObjectMapper objectMapper;

    public List<String> validateForCreate(OrderInfoDTO formData) {
        List<String> errors = new ArrayList<>();

        if (formData == null) {
            errors.add(OrderValidationResult.EMPTY_ITEMS.getMessage());
            return errors;
        }

        validateTableId(formData.getTableId(), true, errors);
        validateCustomerId(formData.getCustomerId(), errors);
        validateNote(formData.getNote(), errors);
        validateAmountIfPresent(formData.getTotalAmount(), OrderValidationResult.INVALID_TOTAL_AMOUNT, errors);
        validateAmountIfPresent(formData.getFinalAmount(), OrderValidationResult.INVALID_FINAL_AMOUNT, errors);
        validateAmountIfPresent(formData.getDiscountAmount(), OrderValidationResult.INVALID_DISCOUNT_AMOUNT, errors);
        validateSelectedDrinks(formData.getEffectiveSelectedDrinksJson(), errors);

        return errors;
    }

    public List<String> validateForUpdate(OrderInfoDTO formData) {
        List<String> errors = new ArrayList<>();

        if (formData == null || formData.getId() == null || formData.getId() <= 0) {
            errors.add(OrderValidationResult.INVALID_ID.getMessage());
            return errors;
        }

        validateTableId(formData.getTableId(), true, errors);
        validateCustomerId(formData.getCustomerId(), errors);
        validateNote(formData.getNote(), errors);
        validateStatusIfPresent(formData.getStatus(), errors);
        validateAmountIfPresent(formData.getTotalAmount(), OrderValidationResult.INVALID_TOTAL_AMOUNT, errors);
        validateAmountIfPresent(formData.getFinalAmount(), OrderValidationResult.INVALID_FINAL_AMOUNT, errors);
        validateAmountIfPresent(formData.getDiscountAmount(), OrderValidationResult.INVALID_DISCOUNT_AMOUNT, errors);
        validateSelectedDrinks(formData.getEffectiveSelectedDrinksJson(), errors);

        return errors;
    }

    public List<String> validateForCancel(Integer id) {
        List<String> errors = new ArrayList<>();
        validateOrderId(id, errors);
        return errors;
    }

    public List<String> validateForCheckout(Integer id, String loyaltyAction, Integer freeDrinkId) {
        List<String> errors = new ArrayList<>();

        validateOrderId(id, errors);
        validateLoyaltyAction(loyaltyAction, freeDrinkId, errors);

        return errors;
    }

    public List<String> validateForChangeStatus(Integer id, OrderStatusEnum status) {
        List<String> errors = new ArrayList<>();

        validateOrderId(id, errors);
        if (status == null) {
            errors.add(OrderValidationResult.REQUIRED_STATUS.getMessage());
        }

        return errors;
    }

    private void validateOrderId(Integer id, List<String> errors) {
        if (id == null || id <= 0) {
            errors.add(OrderValidationResult.INVALID_ID.getMessage());
        }
    }

    private void validateTableId(Integer tableId, boolean required, List<String> errors) {
        if (tableId == null) {
            if (required) {
                errors.add(OrderValidationResult.REQUIRED_TABLE.getMessage());
            }
            return;
        }

        if (tableId <= 0) {
            errors.add(OrderValidationResult.INVALID_TABLE_ID.getMessage());
        }
    }

    private void validateCustomerId(Integer customerId, List<String> errors) {
        if (customerId != null && customerId <= 0) {
            errors.add(OrderValidationResult.INVALID_CUSTOMER_ID.getMessage());
        }
    }

    private void validateNote(String note, List<String> errors) {
        if (note != null && note.trim().length() > MAX_NOTE_LENGTH) {
            errors.add(OrderValidationResult.INVALID_NOTE.getMessage());
        }
    }

    private void validateStatusIfPresent(String status, List<String> errors) {
        if (!StringUtils.hasText(status)) {
            return;
        }

        try {
            OrderStatusEnum.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            errors.add(OrderValidationResult.INVALID_STATUS.getMessage());
        }
    }

    private void validateAmountIfPresent(
            BigDecimal amount,
            OrderValidationResult result,
            List<String> errors
    ) {
        if (amount == null) {
            return;
        }

        if (amount.compareTo(BigDecimal.ZERO) < 0 || amount.compareTo(MAX_AMOUNT) > 0) {
            errors.add(result.getMessage());
        }
    }

    private void validateLoyaltyAction(String loyaltyAction, Integer freeDrinkId, List<String> errors) {
        String action = normalize(loyaltyAction);
        if (action.isEmpty()) {
            action = "earn";
        }

        if (!ALLOWED_LOYALTY_ACTIONS.contains(action)) {
            errors.add(OrderValidationResult.INVALID_LOYALTY_ACTION.getMessage());
            return;
        }

        if ("redeem".equals(action) && (freeDrinkId == null || freeDrinkId <= 0)) {
            errors.add(OrderValidationResult.INVALID_FREE_DRINK_ID.getMessage());
        }
    }

    private void validateSelectedDrinks(String selectedDrinksJson, List<String> errors) {
        if (!StringUtils.hasText(selectedDrinksJson)) {
            errors.add(OrderValidationResult.REQUIRED_DRINK.getMessage());
            return;
        }

        List<SelectedDrinkDTO> selectedDrinks;
        try {
            selectedDrinks = objectMapper.readValue(
                    selectedDrinksJson,
                    new TypeReference<List<SelectedDrinkDTO>>() {
                    }
            );
        } catch (Exception e) {
            errors.add(OrderValidationResult.INVALID_DRINK_ID.getMessage());
            return;
        }

        if (selectedDrinks == null || selectedDrinks.isEmpty()) {
            errors.add(OrderValidationResult.EMPTY_ITEMS.getMessage());
            return;
        }

        Set<Integer> drinkIds = new HashSet<>();
        for (SelectedDrinkDTO selectedDrink : selectedDrinks) {
            Integer drinkId = selectedDrink.getDrinkId();
            Integer quantity = selectedDrink.getQuantity();
            BigDecimal unitPrice = selectedDrink.getUnitPrice();

            if (drinkId == null || drinkId <= 0) {
                errors.add(OrderValidationResult.INVALID_DRINK_ID.getMessage());
                return;
            }

            if (!drinkIds.add(drinkId)) {
                errors.add(OrderValidationResult.DUPLICATE_DRINK.getMessage());
                return;
            }

            if (quantity == null) {
                errors.add(OrderValidationResult.REQUIRED_QUANTITY.getMessage());
                return;
            }

            if (quantity <= 0) {
                errors.add(OrderValidationResult.INVALID_QUANTITY.getMessage());
                return;
            }

            if (unitPrice != null && (unitPrice.compareTo(BigDecimal.ZERO) < 0 || unitPrice.compareTo(MAX_AMOUNT) > 0)) {
                errors.add(OrderValidationResult.INVALID_UNIT_PRICE.getMessage());
                return;
            }
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value.trim().toLowerCase();
    }
}
