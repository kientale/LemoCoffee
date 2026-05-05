package com.kien.keycoffee.service;

import com.kien.keycoffee.dto.TopCustomerStatisticDTO;
import com.kien.keycoffee.dto.TopDrinkStatisticDTO;

import java.math.BigDecimal;
import java.util.List;

public interface StatisticService {

    BigDecimal getRevenueToday();

    BigDecimal getRevenueThisMonth();

    BigDecimal getRevenueThisYear();

    List<TopCustomerStatisticDTO> getTopCustomers(int limit);

    List<TopDrinkStatisticDTO> getTopDrinks(int limit);
}
