package com.kien.keycoffee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelectedIngredientDTO {
    private Integer id;
    private String name;
    private String unit;
    private BigDecimal quantity;
}
