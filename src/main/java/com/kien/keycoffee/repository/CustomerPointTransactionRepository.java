package com.kien.keycoffee.repository;

import com.kien.keycoffee.constant.CustomerPointTransactionTypeEnum;
import com.kien.keycoffee.entity.CustomerPointTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerPointTransactionRepository extends JpaRepository<CustomerPointTransaction, Integer> {

    List<CustomerPointTransaction> findByCustomerIdOrderByCreatedAtDesc(Integer customerId);

    Page<CustomerPointTransaction> findByCustomerIdOrderByCreatedAtDesc(Integer customerId, Pageable pageable);

    List<CustomerPointTransaction> findByOrderIdOrderByCreatedAtDesc(Integer orderId);

    boolean existsByOrderIdAndType(Integer orderId, CustomerPointTransactionTypeEnum type);
}
