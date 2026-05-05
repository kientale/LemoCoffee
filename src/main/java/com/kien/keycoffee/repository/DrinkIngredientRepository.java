package com.kien.keycoffee.repository;

import com.kien.keycoffee.entity.DrinkIngredient;
import com.kien.keycoffee.entity.DrinkIngredientId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DrinkIngredientRepository extends JpaRepository<DrinkIngredient, DrinkIngredientId> {

    List<DrinkIngredient> findByIdDrinkIdOrderByIngredientNameAsc(Integer drinkId);

    void deleteByIdDrinkId(Integer drinkId);
}
