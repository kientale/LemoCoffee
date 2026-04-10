package com.kien.lemocoffee.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class DrinkIngredientId implements Serializable {

    @Column(name = "drink_id")
    private Integer drinkId;

    @Column(name = "ingredient_id")
    private Integer ingredientId;
}