package com.kien.keycoffee.service;

import com.kien.keycoffee.entity.CustomerPointTransaction;

import java.util.List;

public interface CustomerPointTransactionService {

    List<CustomerPointTransaction> getTransactionsByCustomerId(Integer customerId);
}
