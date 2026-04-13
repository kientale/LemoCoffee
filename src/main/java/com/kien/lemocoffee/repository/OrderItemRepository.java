package com.kien.lemocoffee.repository;

import com.kien.lemocoffee.dto.TopDrinkStatisticProjection;
import com.kien.lemocoffee.entity.OrderItem;
import io.micrometer.common.lang.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {

    @NonNull
    Optional<OrderItem> findById(@NonNull Integer id);

    List<OrderItem> findByOrderIdOrderByIdAsc(Integer orderId);

    boolean existsByOrderId(Integer orderId);

    void deleteByOrderId(Integer orderId);

    @Query(
            value = """
                    select
                        oi.drink_id as drinkId,
                        oi.drink_name as drinkName,
                        coalesce(sum(oi.quantity), 0) as totalQuantity,
                        coalesce(sum(oi.subtotal), 0) as totalRevenue
                    from order_item oi
                    join coffee_order o on o.id = oi.order_id
                    where o.status = 'COMPLETED'
                    group by oi.drink_id, oi.drink_name
                    order by totalQuantity desc, totalRevenue desc, oi.drink_id desc
                    """,
            nativeQuery = true
    )
    List<TopDrinkStatisticProjection> findTopBestSellingDrinks(Pageable pageable);
}
