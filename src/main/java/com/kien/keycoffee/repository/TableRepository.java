package com.kien.keycoffee.repository;

import com.kien.keycoffee.entity.CoffeeTable;
import com.kien.keycoffee.constant.TableStatusEnum;
import io.micrometer.common.lang.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TableRepository extends JpaRepository<CoffeeTable,Integer> {

    Page<CoffeeTable> findByStatusNot(TableStatusEnum status, Pageable pageable);

    Page<CoffeeTable> findByStatus(TableStatusEnum status, Pageable pageable);

    Page<CoffeeTable> findByTableNameContainingIgnoreCaseAndStatusNot(
            String keyword,
            TableStatusEnum status,
            Pageable pageable
    );

    Page<CoffeeTable> findByTableNameContainingIgnoreCaseAndStatus(
            String keyword,
            TableStatusEnum status,
            Pageable pageable
    );

    @NonNull
    Optional<CoffeeTable> findById(@NonNull Integer id);

    Optional<CoffeeTable> findTopByOrderByIdDesc();
}
