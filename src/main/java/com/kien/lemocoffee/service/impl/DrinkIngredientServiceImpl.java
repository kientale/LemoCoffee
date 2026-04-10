package com.kien.lemocoffee.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kien.lemocoffee.entity.Drink;
import com.kien.lemocoffee.entity.DrinkIngredient;
import com.kien.lemocoffee.entity.DrinkIngredientId;
import com.kien.lemocoffee.entity.Ingredient;
import com.kien.lemocoffee.repository.DrinkIngredientRepository;
import com.kien.lemocoffee.repository.WarehouseRepository;
import com.kien.lemocoffee.service.DrinkIngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DrinkIngredientServiceImpl implements DrinkIngredientService {

    private static final String EMPTY_SELECTED_INGREDIENTS_JSON = "[]";

    private final DrinkIngredientRepository drinkIngredientRepository;
    private final WarehouseRepository warehouseRepository;
    private final ObjectMapper objectMapper;

    @Override
    public List<DrinkIngredient> getDrinkIngredientsByDrinkId(Integer drinkId) {
        if (drinkId == null || drinkId <= 0) {
            return Collections.emptyList();
        }
        return drinkIngredientRepository.findByIdDrinkIdOrderByIngredientNameAsc(drinkId);
    }

    @Override
    public String getSelectedIngredientsJsonByDrinkId(Integer drinkId) {
        List<Map<String, Object>> selectedIngredients = getDrinkIngredientsByDrinkId(drinkId)
                .stream()
                .map(drinkIngredient -> Map.<String, Object>of(
                        "id", drinkIngredient.getId().getIngredientId(),
                        "name", drinkIngredient.getIngredientName(),
                        "unit", drinkIngredient.getUnit(),
                        "quantity", drinkIngredient.getQuantity()
                ))
                .toList();

        try {
            return objectMapper.writeValueAsString(selectedIngredients);
        } catch (Exception e) {
            return EMPTY_SELECTED_INGREDIENTS_JSON;
        }
    }

    @Override
    @Transactional
    public void replaceDrinkIngredients(Drink drink, String selectedIngredientsJson) {
        if (drink == null || drink.getId() == null || drink.getId() <= 0) {
            throw new IllegalArgumentException("Invalid drink");
        }

        List<Map<String, Object>> selectedIngredients = parseSelectedIngredients(selectedIngredientsJson);

        drinkIngredientRepository.deleteByIdDrinkId(drink.getId());
        drinkIngredientRepository.flush();

        List<DrinkIngredient> drinkIngredients = selectedIngredients.stream()
                .map(item -> toDrinkIngredient(drink, item))
                .toList();

        drinkIngredientRepository.saveAllAndFlush(drinkIngredients);
    }

    private List<Map<String, Object>> parseSelectedIngredients(String selectedIngredientsJson) {
        if (!StringUtils.hasText(selectedIngredientsJson)) {
            throw new IllegalArgumentException("Invalid selected ingredients");
        }

        try {
            List<Map<String, Object>> selectedIngredients = objectMapper.readValue(
                    selectedIngredientsJson,
                    new TypeReference<>() {
                    }
            );

            if (selectedIngredients == null || selectedIngredients.isEmpty()) {
                throw new IllegalArgumentException("Invalid selected ingredients");
            }

            return selectedIngredients;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid selected ingredients", e);
        }
    }

    private DrinkIngredient toDrinkIngredient(Drink drink, Map<String, Object> item) {
        Integer ingredientId = toInteger(item.get("id"));
        BigDecimal quantity = toBigDecimal(item.get("quantity"));

        if (ingredientId == null || ingredientId <= 0 || quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid selected ingredient");
        }

        Ingredient ingredient = warehouseRepository.findById(ingredientId)
                .orElseThrow(() -> new IllegalArgumentException("Ingredient not found"));

        return DrinkIngredient.builder()
                .id(new DrinkIngredientId(drink.getId(), ingredient.getId()))
                .drink(drink)
                .ingredient(ingredient)
                .ingredientName(ingredient.getName())
                .quantity(quantity)
                .unit(ingredient.getUnit())
                .build();
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
}
