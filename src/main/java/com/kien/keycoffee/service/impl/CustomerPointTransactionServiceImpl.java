package com.kien.keycoffee.service.impl;

import com.kien.keycoffee.entity.CustomerPointTransaction;
import com.kien.keycoffee.repository.CustomerPointTransactionRepository;
import com.kien.keycoffee.service.CustomerPointTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerPointTransactionServiceImpl implements CustomerPointTransactionService {

    private final CustomerPointTransactionRepository customerPointTransactionRepository;

    @Override
    public List<CustomerPointTransaction> getTransactionsByCustomerId(Integer customerId) {
        if (customerId == null || customerId <= 0) {
            return Collections.emptyList();
        }

        return customerPointTransactionRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }
}
