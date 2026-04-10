package com.kien.lemocoffee.service;

import com.kien.lemocoffee.dto.TopCustomerStatisticDTO;
import com.kien.lemocoffee.dto.TopDrinkStatisticDTO;

import java.math.BigDecimal;
import java.util.List;

public interface StatisticService {

    BigDecimal getRevenueToday();

    BigDecimal getRevenueThisMonth();

    BigDecimal getRevenueThisYear();

    List<TopCustomerStatisticDTO> getTopCustomers(int limit);

    List<TopDrinkStatisticDTO> getTopDrinks(int limit);
}
