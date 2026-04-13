package com.kien.lemocoffee.repository;

import com.kien.lemocoffee.constant.OrderStatusEnum;
import com.kien.lemocoffee.entity.Order;
import io.micrometer.common.lang.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    @NonNull
    Optional<Order> findById(@NonNull Integer id);

    boolean existsByTableIdAndStatus(Integer tableId, OrderStatusEnum status);

    @Query(
            value = """
                    select o.*
                    from coffee_order o
                    left join coffee_table t on t.id = o.table_id
                    where (:keyword is null or :keyword = ''
                           or lower(t.table_name) like lower(concat('%', :keyword, '%')))
                    """,
            countQuery = """
                    select count(*)
                    from coffee_order o
                    left join coffee_table t on t.id = o.table_id
                    where (:keyword is null or :keyword = ''
                           or lower(t.table_name) like lower(concat('%', :keyword, '%')))
                    """,
            nativeQuery = true
    )
    Page<Order> searchByTableName(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query(
            value = """
                    select coalesce(sum(o.final_amount), 0)
                    from coffee_order o
                    where o.status = :status
                      and o.created_at >= :fromDate
                      and o.created_at < :toDate
                    """,
            nativeQuery = true
    )
    BigDecimal sumRevenueByStatusAndCreatedAtRange(
            @Param("status") String status,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );
}
