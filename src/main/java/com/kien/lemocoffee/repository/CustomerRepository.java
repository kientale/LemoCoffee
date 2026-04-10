package com.kien.lemocoffee.repository;

import com.kien.lemocoffee.entity.Customer;
import com.kien.lemocoffee.constant.CustomerStatusEnum;
import com.kien.lemocoffee.dto.TopCustomerStatisticProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    Page<Customer> findByStatusNot(CustomerStatusEnum status, Pageable pageable);

    Page<Customer> findByFullNameContainingIgnoreCaseAndStatusNot(
            String keyword,
            CustomerStatusEnum status,
            Pageable pageable
    );

    Optional<Customer> findById(Integer id);

    Optional<Customer> findByPhone(String phone);

    boolean existsByPhone(String phone);

    boolean existsByPhoneAndIdNot(String phone, Integer id);

    @Query(
            value = """
                    select
                        c.id as customerId,
                        c.full_name as customerName,
                        c.phone as customerPhone,
                        coalesce(c.points, 0) as currentPoints,
                        count(o.id) as totalOrders,
                        coalesce(sum(coalesce(o.final_amount, o.total_amount, 0)), 0) as totalSpent
                    from customer c
                    join coffee_order o on o.customer_id = c.id
                    where o.status = 'COMPLETED'
                    group by c.id, c.full_name, c.phone, c.points
                    order by totalSpent desc, totalOrders desc, c.id desc
                    """,
            nativeQuery = true
    )
    List<TopCustomerStatisticProjection> findTopLoyalCustomers(Pageable pageable);
}
