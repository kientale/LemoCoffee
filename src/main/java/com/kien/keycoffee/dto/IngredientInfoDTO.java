package com.kien.keycoffee.dto;

import com.kien.keycoffee.constant.IngredientStatusEnum;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngredientInfoDTO {
    private Integer id;

    private String name;
    private BigDecimal quantity;
    private String description;
    private String unit;
    private String supplier;

    private IngredientStatusEnum status;
}
