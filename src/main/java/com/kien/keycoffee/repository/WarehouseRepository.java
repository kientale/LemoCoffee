package com.kien.keycoffee.repository;

import com.kien.keycoffee.constant.IngredientStatusEnum;
import com.kien.keycoffee.entity.Ingredient;
import jakarta.persistence.LockModeType;
import io.micrometer.common.lang.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Ingredient i where i.id in :ids")
    List<Ingredient> findAllByIdInForUpdate(@Param("ids") Collection<Integer> ids);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Integer id);
}
