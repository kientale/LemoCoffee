package com.kien.keycoffee.dto;

import com.kien.keycoffee.constant.DrinkStatusEnum;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DrinkInfoDTO {
    private Integer id;

    private String name;
    private BigDecimal price;
    private String description;
    private String image;
    private String selectedIngredientsJson;

    private DrinkStatusEnum status;
}
