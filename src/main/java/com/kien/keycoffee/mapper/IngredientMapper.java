package com.kien.keycoffee.mapper;

import com.kien.keycoffee.dto.IngredientInfoDTO;
import com.kien.keycoffee.entity.Ingredient;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface IngredientMapper {

    IngredientInfoDTO toIngredientInfoDTO(Ingredient ingredient);
}
