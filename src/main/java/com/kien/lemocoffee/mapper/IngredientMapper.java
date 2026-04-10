package com.kien.lemocoffee.mapper;

import com.kien.lemocoffee.dto.IngredientInfoDTO;
import com.kien.lemocoffee.entity.Ingredient;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface IngredientMapper {

    IngredientInfoDTO toIngredientInfoDTO(Ingredient ingredient);
}
