package com.kien.lemocoffee.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kien.lemocoffee.constant.DrinkStatusEnum;
import com.kien.lemocoffee.dto.OrderItemDTO;
import com.kien.lemocoffee.entity.Drink;
import com.kien.lemocoffee.entity.Order;
import com.kien.lemocoffee.entity.OrderItem;
import com.kien.lemocoffee.mapper.OrderMapper;
import com.kien.lemocoffee.repository.DrinkRepository;
import com.kien.lemocoffee.repository.OrderItemRepository;
import com.kien.lemocoffee.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderItemServiceImpl implements OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final DrinkRepository drinkRepository;
    private final OrderMapper orderMapper;
    private final ObjectMapper objectMapper;

    @Override
    public List<OrderItem> findItemsByOrderId(Integer orderId) {
        if (isInvalidId(orderId)) {
            return Collections.emptyList();
        }

        return orderItemRepository.findByOrderIdOrderByIdAsc(orderId);
    }

    @Override
    public List<OrderItemDTO> getItemDTOsByOrderId(Integer orderId) {
        return orderMapper.toOrderItemDTOs(findItemsByOrderId(orderId));
    }

    @Override
    @Transactional
    public BigDecimal replaceOrderItems(Order order, String selectedDrinksJson) {
        if (order == null || isInvalidId(order.getId())) {
            throw new IllegalArgumentException("Invalid order");
        }

        List<SelectedDrink> selectedDrinks = parseSelectedDrinks(selectedDrinksJson);
        if (selectedDrinks.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one drink");
        }

        orderItemRepository.deleteByOrderId(order.getId());

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (SelectedDrink selectedDrink : selectedDrinks) {
            Drink drink = drinkRepository.findById(selectedDrink.drinkId()).orElse(null);
            if (drink == null || drink.getStatus() != DrinkStatusEnum.AVAILABLE) {
                throw new IllegalArgumentException("Drink is not available");
            }

            BigDecimal unitPrice = drink.getPrice() == null ? BigDecimal.ZERO : drink.getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(selectedDrink.quantity()));

            orderItems.add(OrderItem.builder()
                    .orderId(order.getId())
                    .drinkId(drink.getId())
                    .drinkName(drink.getName())
                    .quantity(selectedDrink.quantity())
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .pricingType("NORMAL")
                    .pointsRedeemed(0)
                    .build());

            totalAmount = totalAmount.add(subtotal);
        }

        orderItemRepository.saveAll(orderItems);
        return totalAmount;
    }

    @Override
    @Transactional
    public void deleteItemsByOrderId(Integer orderId) {
        if (isInvalidId(orderId)) {
            return;
        }

        orderItemRepository.deleteByOrderId(orderId);
    }

    @Override
    public boolean hasItems(Integer orderId) {
        return !isInvalidId(orderId) && orderItemRepository.existsByOrderId(orderId);
    }

    private List<SelectedDrink> parseSelectedDrinks(String selectedDrinksJson) {
        List<Map<String, Object>> rawItems;
        try {
            rawItems = objectMapper.readValue(
                    selectedDrinksJson == null ? "[]" : selectedDrinksJson,
                    new TypeReference<>() {
                    }
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid selected drinks JSON", e);
        }

        if (rawItems == null || rawItems.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Integer> drinkIds = new HashSet<>();
        List<SelectedDrink> selectedDrinks = new ArrayList<>();

        for (Map<String, Object> item : rawItems) {
            Integer drinkId = firstInteger(item, "drinkId", "id");
            Integer quantity = firstInteger(item, "quantity", "qty");

            if (drinkId == null || drinkId <= 0 || quantity == null || quantity <= 0) {
                throw new IllegalArgumentException("Invalid selected drink");
            }

            if (!drinkIds.add(drinkId)) {
                throw new IllegalArgumentException("Duplicate selected drink");
            }

            selectedDrinks.add(new SelectedDrink(drinkId, quantity));
        }

        return selectedDrinks;
    }

    private Integer firstInteger(Map<String, Object> source, String... keys) {
        for (String key : keys) {
            Integer value = toInteger(source.get(key));
            if (value != null) {
                return value;
            }
        }

        return null;
    }

    private Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {
            return number.intValue();
        }

        try {
            return Integer.valueOf(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isInvalidId(Integer id) {
        return id == null || id <= 0;
    }

    private record SelectedDrink(Integer drinkId, Integer quantity) {
    }
}
