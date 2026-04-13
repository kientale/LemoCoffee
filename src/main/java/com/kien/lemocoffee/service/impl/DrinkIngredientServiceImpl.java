package com.kien.lemocoffee.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kien.lemocoffee.dto.SelectedIngredientDTO;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        List<SelectedIngredientDTO> selectedIngredients = getDrinkIngredientsByDrinkId(drinkId)
                .stream()
                .map(this::toSelectedIngredientDTO)
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

        List<SelectedIngredientDTO> selectedIngredients = parseSelectedIngredients(selectedIngredientsJson);

        drinkIngredientRepository.deleteByIdDrinkId(drink.getId());
        drinkIngredientRepository.flush();

        List<DrinkIngredient> drinkIngredients = selectedIngredients.stream()
                .map(selectedIngredient -> toDrinkIngredient(drink, selectedIngredient))
                .toList();

        drinkIngredientRepository.saveAllAndFlush(drinkIngredients);
    }

    private List<SelectedIngredientDTO> parseSelectedIngredients(String selectedIngredientsJson) {
        if (!StringUtils.hasText(selectedIngredientsJson)) {
            throw new IllegalArgumentException("Invalid selected ingredients");
        }

        try {
            List<SelectedIngredientDTO> selectedIngredients = objectMapper.readValue(
                    selectedIngredientsJson,
                    new TypeReference<>() {
                    }
            );

            if (selectedIngredients == null || selectedIngredients.isEmpty()) {
                throw new IllegalArgumentException("Invalid selected ingredients");
            }

            validateSelectedIngredients(selectedIngredients);
            return selectedIngredients;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid selected ingredients", e);
        }
    }

    private SelectedIngredientDTO toSelectedIngredientDTO(DrinkIngredient drinkIngredient) {
        return SelectedIngredientDTO.builder()
                .id(drinkIngredient.getId().getIngredientId())
                .name(drinkIngredient.getIngredientName())
                .unit(drinkIngredient.getUnit())
                .quantity(drinkIngredient.getQuantity())
                .build();
    }

    private DrinkIngredient toDrinkIngredient(Drink drink, SelectedIngredientDTO selectedIngredient) {
        Integer ingredientId = selectedIngredient.getId();

        Ingredient ingredient = warehouseRepository.findById(ingredientId)
                .orElseThrow(() -> new IllegalArgumentException("Ingredient not found"));

        return DrinkIngredient.builder()
                .id(new DrinkIngredientId(drink.getId(), ingredient.getId()))
                .drink(drink)
                .ingredient(ingredient)
                .ingredientName(ingredient.getName())
                .quantity(selectedIngredient.getQuantity())
                .unit(ingredient.getUnit())
                .build();
    }

    private void validateSelectedIngredients(List<SelectedIngredientDTO> selectedIngredients) {
        Set<Integer> ingredientIds = new HashSet<>();
        for (SelectedIngredientDTO selectedIngredient : selectedIngredients) {
            Integer ingredientId = selectedIngredient.getId();
            BigDecimal quantity = selectedIngredient.getQuantity();

            if (ingredientId == null || ingredientId <= 0 || quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Invalid selected ingredient");
            }

            if (!ingredientIds.add(ingredientId)) {
                throw new IllegalArgumentException("Duplicate selected ingredient");
            }
        }
    }
}
