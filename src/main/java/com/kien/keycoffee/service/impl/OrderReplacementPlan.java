package com.kien.keycoffee.service.impl;

import com.kien.keycoffee.entity.OrderItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

record OrderReplacementPlan(List<OrderItem> orderItems, BigDecimal totalAmount,
                            Map<Integer, BigDecimal> requiredIngredients) {
}
