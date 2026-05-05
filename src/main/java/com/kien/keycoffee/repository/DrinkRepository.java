package com.kien.keycoffee.repository;

import com.kien.keycoffee.constant.DrinkStatusEnum;
import com.kien.keycoffee.entity.Drink;
import io.micrometer.common.lang.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DrinkRepository extends JpaRepository<Drink, Integer> {

    Page<Drink> findByStatus(DrinkStatusEnum status, Pageable pageable);

    Page<Drink> findByStatusNot(DrinkStatusEnum status, Pageable pageable);

    Page<Drink> findByNameContainingIgnoreCaseAndStatus(
            String keyword,
            DrinkStatusEnum status,
            Pageable pageable
    );

    Page<Drink> findByNameContainingIgnoreCaseAndStatusNot(
            String keyword,
            DrinkStatusEnum status,
            Pageable pageable
    );

    @NonNull
    Optional<Drink> findById(@NonNull Integer id);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Integer id);
}
