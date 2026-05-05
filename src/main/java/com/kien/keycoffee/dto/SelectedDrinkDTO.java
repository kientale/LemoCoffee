package com.kien.keycoffee.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class SelectedDrinkDTO {

    @JsonAlias("id")
    private Integer drinkId;

    @JsonAlias("name")
    private String drinkName;

    @JsonAlias("price")
    private BigDecimal unitPrice;

    private Integer quantity;
}
