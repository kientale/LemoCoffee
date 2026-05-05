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
public class TopDrinkStatisticDTO {

    private Integer drinkId;
    private String drinkName;
    private Long totalQuantity;
    private BigDecimal totalRevenue;
}
