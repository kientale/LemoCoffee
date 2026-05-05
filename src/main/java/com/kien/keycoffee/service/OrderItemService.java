package com.kien.keycoffee.service;

import com.kien.keycoffee.entity.Order;
import com.kien.keycoffee.entity.OrderItem;

import java.math.BigDecimal;
import java.util.List;

public interface OrderItemService {

    List<OrderItem> findItemsByOrderId(Integer orderId);

    String getSelectedDrinksJsonByOrderId(Integer orderId);

    BigDecimal replaceOrderItems(Order order, String selectedDrinksJson);

    void restoreIngredientStockByOrderId(Integer orderId);
}
