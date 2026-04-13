package com.kien.lemocoffee.service;

import com.kien.lemocoffee.dto.OrderItemDTO;
import com.kien.lemocoffee.entity.Order;
import com.kien.lemocoffee.entity.OrderItem;

import java.math.BigDecimal;
import java.util.List;

public interface OrderItemService {

    List<OrderItem> findItemsByOrderId(Integer orderId);

    BigDecimal replaceOrderItems(Order order, String selectedDrinksJson);
}
