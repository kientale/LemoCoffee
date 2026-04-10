package com.kien.lemocoffee.repository;

import com.kien.lemocoffee.constant.IngredientStatusEnum;
import com.kien.lemocoffee.entity.Ingredient;
import io.micrometer.common.lang.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Ingredient, Integer> {

    Page<Ingredient> findByStatusNot(IngredientStatusEnum status, Pageable pageable);

    Page<Ingredient> findByNameContainingIgnoreCaseAndStatusNot(
            String keyword,
            IngredientStatusEnum status,
            Pageable pageable
    );

    Page<Ingredient> findBySupplierContainingIgnoreCaseAndStatusNot(
            String keyword,
            IngredientStatusEnum status,
            Pageable pageable
    );

    Page<Ingredient> findByUnitContainingIgnoreCaseAndStatusNot(
            String keyword,
            IngredientStatusEnum status,
            Pageable pageable
    );

    @NonNull
    Optional<Ingredient> findById(@NonNull Integer id);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Integer id);
}
