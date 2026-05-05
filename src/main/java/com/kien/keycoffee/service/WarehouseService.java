package com.kien.keycoffee.service;

import com.kien.keycoffee.constant.IngredientStatusEnum;
import com.kien.keycoffee.constant.WarehouseManagementResult;
import com.kien.keycoffee.dto.IngredientInfoDTO;
import org.springframework.data.domain.Page;

public interface WarehouseService {

    Page<IngredientInfoDTO> getIngredient(int page, int size, String keyword, String field);

    WarehouseManagementResult createIngredient(IngredientInfoDTO formData);

    IngredientInfoDTO getIngredientInfoById(Integer id);

    WarehouseManagementResult updateIngredient(IngredientInfoDTO formData);

    WarehouseManagementResult deleteIngredient(Integer id, IngredientStatusEnum status);
}
