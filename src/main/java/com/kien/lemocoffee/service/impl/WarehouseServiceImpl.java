package com.kien.lemocoffee.service.impl;

import com.kien.lemocoffee.constant.IngredientStatusEnum;
import com.kien.lemocoffee.constant.WarehouseManagementResult;
import com.kien.lemocoffee.dto.IngredientInfoDTO;
import com.kien.lemocoffee.entity.Ingredient;
import com.kien.lemocoffee.mapper.IngredientMapper;
import com.kien.lemocoffee.repository.WarehouseRepository;
import com.kien.lemocoffee.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final IngredientMapper ingredientMapper;

    @Override
    public Page<IngredientInfoDTO> getIngredient(int page, int size, String keyword, String field) {

        int pageNo = Math.max(1, page);
        int pageSize = Math.max(1, size);
        String kw = normalize(keyword);
        String fd = normalize(field).toLowerCase();

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, Sort.by(Sort.Direction.DESC, "id"));
        Page<Ingredient> ingredientPage;

        if (kw.isEmpty()) {
            ingredientPage = warehouseRepository.findByStatusNot(IngredientStatusEnum.DELETED, pageable);
        } else if ("supplier".equals(fd)) {
            ingredientPage = warehouseRepository.findBySupplierContainingIgnoreCaseAndStatusNot(
                    kw,
                    IngredientStatusEnum.DELETED,
                    pageable
            );
        } else if ("unit".equals(fd)) {
            ingredientPage = warehouseRepository.findByUnitContainingIgnoreCaseAndStatusNot(
                    kw,
                    IngredientStatusEnum.DELETED,
                    pageable
            );
        } else {
            ingredientPage = warehouseRepository.findByNameContainingIgnoreCaseAndStatusNot(
                    kw,
                    IngredientStatusEnum.DELETED,
                    pageable
            );
        }

        return ingredientPage.map(ingredientMapper::toIngredientInfoDTO);
    }

    @Override
    @Transactional
    public WarehouseManagementResult createIngredient(IngredientInfoDTO formData) {
        try {
            if (warehouseRepository.existsByNameIgnoreCase(formData.getName())) {
                return WarehouseManagementResult.INGREDIENT_ALREADY_EXISTS;
            }

            Ingredient ingredient = Ingredient.builder()
                    .name(formData.getName())
                    .quantity(formData.getQuantity())
                    .description(formData.getDescription())
                    .unit(formData.getUnit())
                    .supplier(formData.getSupplier())
                    .status(IngredientStatusEnum.ACTIVE)
                    .build();

            warehouseRepository.save(ingredient);
            return WarehouseManagementResult.CREATE_SUCCESS;

        } catch (Exception e) {
            log.error("Failed to create ingredient with name={}", formData.getName(), e);
            return WarehouseManagementResult.CREATE_FAILED;
        }
    }

    @Override
    public IngredientInfoDTO getIngredientInfoById(Integer id) {
        return warehouseRepository.findById(id)
                .map(ingredientMapper::toIngredientInfoDTO)
                .orElse(null);
    }

    @Override
    @Transactional
    public WarehouseManagementResult updateIngredient(IngredientInfoDTO formData) {
        try {
            Integer id = formData.getId();
            Ingredient ingredient = findIngredientById(id);

            if (ingredient == null) {
                return WarehouseManagementResult.INGREDIENT_NOT_FOUND;
            }

            if (warehouseRepository.existsByNameIgnoreCaseAndIdNot(formData.getName(), id)) {
                return WarehouseManagementResult.INGREDIENT_ALREADY_EXISTS;
            }

            ingredient.setName(formData.getName());
            ingredient.setQuantity(formData.getQuantity());
            ingredient.setDescription(formData.getDescription());
            ingredient.setUnit(formData.getUnit());
            ingredient.setSupplier(formData.getSupplier());
            ingredient.setStatus(formData.getStatus());

            warehouseRepository.save(ingredient);
            return WarehouseManagementResult.UPDATE_SUCCESS;

        } catch (Exception e) {
            log.error("Failed to update ingredient with id={}", formData.getId(), e);
            return WarehouseManagementResult.UPDATE_FAILED;
        }
    }

    @Override
    @Transactional
    public WarehouseManagementResult deleteIngredient(Integer id, IngredientStatusEnum status) {
        try {
            if (status == null) {
                return WarehouseManagementResult.DELETE_FAILED;
            }

            Ingredient ingredient = findIngredientById(id);

            if (ingredient == null) {
                return WarehouseManagementResult.INGREDIENT_NOT_FOUND;
            }

            ingredient.setStatus(status);
            warehouseRepository.save(ingredient);
            return WarehouseManagementResult.DELETE_SUCCESS;

        } catch (Exception e) {
            log.error("Failed to delete ingredient id={}", id, e);
            return WarehouseManagementResult.DELETE_FAILED;
        }
    }


    private Ingredient findIngredientById(Integer id) {
        if (id == null || id <= 0) {
            return null;
        }
        return warehouseRepository.findById(id).orElse(null);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
