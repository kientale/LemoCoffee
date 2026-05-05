package com.kien.keycoffee.dto;

import java.math.BigDecimal;

public interface TopCustomerStatisticProjection {

    Integer getCustomerId();

    String getCustomerName();

    String getCustomerPhone();

    Integer getCurrentPoints();

    Long getTotalOrders();

    BigDecimal getTotalSpent();
}
