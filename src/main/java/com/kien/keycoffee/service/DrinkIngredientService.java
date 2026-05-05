package com.kien.keycoffee.service;

import com.kien.keycoffee.entity.Drink;
import com.kien.keycoffee.entity.DrinkIngredient;

import java.util.List;

public interface DrinkIngredientService {

    List<DrinkIngredient> getDrinkIngredientsByDrinkId(Integer drinkId);

    String getSelectedIngredientsJsonByDrinkId(Integer drinkId);

    void replaceDrinkIngredients(Drink drink, String selectedIngredientsJson);
}
