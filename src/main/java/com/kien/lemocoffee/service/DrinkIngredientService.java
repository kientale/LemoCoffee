package com.kien.lemocoffee.service;

import com.kien.lemocoffee.entity.Drink;
import com.kien.lemocoffee.entity.DrinkIngredient;

import java.util.List;

public interface DrinkIngredientService {

    List<DrinkIngredient> getDrinkIngredientsByDrinkId(Integer drinkId);

    String getSelectedIngredientsJsonByDrinkId(Integer drinkId);

    void replaceDrinkIngredients(Drink drink, String selectedIngredientsJson);
}
