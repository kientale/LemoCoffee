package com.kien.keycoffee.mapper;

import com.kien.keycoffee.dto.DrinkInfoDTO;
import com.kien.keycoffee.dto.DrinkTableDTO;
import com.kien.keycoffee.entity.Drink;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface DrinkMapper {

    DrinkInfoDTO toDrinkInfoDTO(Drink drink);

    DrinkTableDTO toDrinkTableDTO(Drink drink);
}
