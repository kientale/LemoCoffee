package com.kien.lemocoffee.mapper;

import com.kien.lemocoffee.dto.DrinkInfoDTO;
import com.kien.lemocoffee.dto.DrinkTableDTO;
import com.kien.lemocoffee.entity.Drink;
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
