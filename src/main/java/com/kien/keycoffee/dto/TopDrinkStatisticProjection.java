package com.kien.keycoffee.dto;

import java.math.BigDecimal;

public interface TopDrinkStatisticProjection {

    Integer getDrinkId();

    String getDrinkName();

    Long getTotalQuantity();

    BigDecimal getTotalRevenue();
}
